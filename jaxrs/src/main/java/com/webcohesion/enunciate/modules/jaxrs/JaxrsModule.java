package com.webcohesion.enunciate.modules.jaxrs;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.lang.model.element.Element;
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.TypeMirror;
import javax.ws.rs.ApplicationPath;
import javax.ws.rs.Path;
import javax.ws.rs.ext.Provider;

import org.reflections.adapters.MetadataAdapter;

import com.webcohesion.enunciate.EnunciateContext;
import com.webcohesion.enunciate.EnunciateException;
import com.webcohesion.enunciate.api.ApiRegistry;
import com.webcohesion.enunciate.metadata.ServiceContextRoot;
import com.webcohesion.enunciate.module.ApiFeatureProviderModule;
import com.webcohesion.enunciate.module.ApiRegistryProviderModule;
import com.webcohesion.enunciate.module.BasicEnunicateModule;
import com.webcohesion.enunciate.module.DependencySpec;
import com.webcohesion.enunciate.module.EnunciateModule;
import com.webcohesion.enunciate.module.MediaTypeDefinitionModule;
import com.webcohesion.enunciate.module.TypeFilteringModule;
import com.webcohesion.enunciate.modules.jaxrs.model.ResourceEntityParameter;
import com.webcohesion.enunciate.modules.jaxrs.model.ResourceMethod;
import com.webcohesion.enunciate.modules.jaxrs.model.ResourceRepresentationMetadata;
import com.webcohesion.enunciate.modules.jaxrs.model.RootResource;

/**
 * @author Ryan Heaton
 */
@SuppressWarnings("unchecked")
public class JaxrsModule extends BasicEnunicateModule
		implements TypeFilteringModule, ApiRegistryProviderModule, ApiFeatureProviderModule {

	private DataTypeDetectionStrategy defaultDataTypeDetectionStrategy;
	private final List<MediaTypeDefinitionModule> mediaTypeModules = new ArrayList<MediaTypeDefinitionModule>();
	private ApiRegistry apiRegistry;
	static final String NAME = "jaxrs";

	@Override
	public String getName() {
		return NAME;
	}

	@Override
	public List<DependencySpec> getDependencySpecifications() {
		return Arrays.asList((DependencySpec) new MediaTypeDependencySpec());
	}

	public DataTypeDetectionStrategy getDataTypeDetectionStrategy() {
		String dataTypeDetection = this.config.getString("[@datatype-detection]", null);

		if (dataTypeDetection != null) {
			try {
				return DataTypeDetectionStrategy.valueOf(dataTypeDetection);
			} catch (IllegalArgumentException e) {
				// fall through...
			}
		}

		if (this.defaultDataTypeDetectionStrategy != null) {
			return this.defaultDataTypeDetectionStrategy;
		}

		if (this.enunciate.getIncludePatterns().isEmpty()) {
			// if there are no configured include patterns, then we'll just
			// stick with "local" detection so we don't include too much.
			return DataTypeDetectionStrategy.local;
		} else {
			// otherwise, we'll assume the user knows what (s)he's doing and
			// aggressively include everything.
			return DataTypeDetectionStrategy.aggressive;
		}
	}

	public void setDefaultDataTypeDetectionStrategy(DataTypeDetectionStrategy strategy) {
		this.defaultDataTypeDetectionStrategy = strategy;
	}

	@Override
	public void setApiRegistry(ApiRegistry registry) {
		this.apiRegistry = registry;
	}

	public EnunciateJaxrsContext getJaxrsContext() {
		return new EnunciateJaxrsContext(context);
	}

	@Override
	public void call(EnunciateContext context) {

		DataTypeDetectionStrategy detectionStrategy = getDataTypeDetectionStrategy();
		Map<String, EnunciateJaxrsContext> contextMap = new HashMap<String, EnunciateJaxrsContext>();
		if (detectionStrategy != DataTypeDetectionStrategy.passive) {
			Set<? extends Element> elements = detectionStrategy == DataTypeDetectionStrategy.local
					? context.getLocalApiElements() : context.getApiElements();
			for (Element declaration : elements) {
				EnunciateJaxrsContext jaxrsContext = null;
				String relativeContextPath = null;
				if (declaration instanceof TypeElement) {
					TypeElement element = (TypeElement) declaration;

					if ("org.glassfish.jersey.server.wadl.internal.WadlResource"
							.equals(element.getQualifiedName().toString())) {
						// known internal wadl resource not to be documented.
						continue;
					}

					ApplicationPath applicationPathInfo = declaration.getAnnotation(ApplicationPath.class);
					if (applicationPathInfo != null && !applicationPathInfo.value().trim().isEmpty()) {
						relativeContextPath = applicationPathInfo.value();
					}

					ServiceContextRoot serviceCtx = declaration.getAnnotation(ServiceContextRoot.class);
					if (serviceCtx != null) {
						relativeContextPath = serviceCtx.context();
					} 

					if (relativeContextPath == null) {
						// tidy up the application path.
						relativeContextPath = this.config.getString("application[@path]", relativeContextPath);
					}

					if (relativeContextPath != null) {
						if (contextMap.containsKey(relativeContextPath)) {
							jaxrsContext = contextMap.get(serviceCtx);
						} else {
							jaxrsContext = new EnunciateJaxrsContext(context);
							contextMap.put(relativeContextPath, jaxrsContext);
						}
					}

					Provider providerInfo = declaration.getAnnotation(Provider.class);
					if (providerInfo != null && jaxrsContext != null) {
						// add jax-rs provider
						jaxrsContext.addJAXRSProvider(element);
					}

					Path pathInfo = declaration.getAnnotation(Path.class);
					if (pathInfo != null && jaxrsContext != null) {
						// add root resource.
						RootResource rootResource = new RootResource(element, jaxrsContext);
						jaxrsContext.add(rootResource);
						LinkedList<Element> contextStack = new LinkedList<Element>();
						contextStack.push(rootResource);
						try {
							for (ResourceMethod resourceMethod : rootResource.getResourceMethods(true)) {
								addReferencedDataTypeDefinitions(resourceMethod, contextStack);
							}
						} finally {
							contextStack.pop();
						}
					}

					if (relativeContextPath != null && jaxrsContext != null) {
						jaxrsContext.setRelativeContextPath(relativeContextPath);
						jaxrsContext.setGroupingStrategy(getGroupingStrategy());
						if (!contextMap.containsKey(relativeContextPath)) {
							contextMap.put(relativeContextPath, jaxrsContext);
						}

						// while (relativeContextPath.startsWith("/")) {
						// relativeContextPath =
						// relativeContextPath.substring(1);
						// }

						// while (relativeContextPath.endsWith("/")) {
						// // trim off any leading slashes
						// relativeContextPath =
						// relativeContextPath.substring(0,
						// relativeContextPath.length() - 1);
						// }

						// jaxrsContext.setRelativeContextPath(relativeContextPath);

					}
				}
			}
			for (EnunciateJaxrsContext ctx : contextMap.values()) {
				if (ctx.getRootResources().size() > 0) {
					this.enunciate.addArtifact(new JaxrsRootResourceClassListArtifact(ctx));
					this.apiRegistry.getResourceApis().add(ctx);
				}
				if (ctx.getProviders().size() > 0) {
					this.enunciate.addArtifact(new JaxrsProviderClassListArtifact(ctx));
				}
			}

		}
	}

	/**
	 * Add the referenced type definitions for the specified resource method.
	 *
	 * @param resourceMethod
	 *            The resource method.
	 */
	protected void addReferencedDataTypeDefinitions(ResourceMethod resourceMethod, LinkedList<Element> contextStack) {
		ResourceEntityParameter ep = resourceMethod.getEntityParameter();
		if (ep != null) {
			Set<String> consumes = resourceMethod.getConsumesMediaTypes();
			contextStack.push(ep.getDelegate());

			TypeMirror type = ep.getType();
			contextStack.push(resourceMethod);
			try {
				for (MediaTypeDefinitionModule mediaTypeModule : this.mediaTypeModules) {
					mediaTypeModule.addDataTypeDefinitions(type, consumes, contextStack);
				}
			} finally {
				contextStack.pop();
			}
		}

		ResourceRepresentationMetadata outputPayload = resourceMethod.getRepresentationMetadata();
		if (outputPayload != null) {
			TypeMirror type = outputPayload.getDelegate();
			Set<String> produces = resourceMethod.getProducesMediaTypes();
			contextStack.push(resourceMethod);

			try {
				for (MediaTypeDefinitionModule mediaTypeModule : this.mediaTypeModules) {
					mediaTypeModule.addDataTypeDefinitions(type, produces, contextStack);
				}
			} finally {
				contextStack.pop();
			}
		}

		// todo: include referenced type definitions from the errors?
	}

	public EnunciateJaxrsContext.GroupingStrategy getGroupingStrategy() {
		String groupBy = this.config.getString("[@groupBy]", "class");
		if ("class".equals(groupBy)) {
			return EnunciateJaxrsContext.GroupingStrategy.resource_class;
		} else if ("path".equals(groupBy)) {
			return EnunciateJaxrsContext.GroupingStrategy.path;
		} else if ("annotation".equals(groupBy)) {
			return EnunciateJaxrsContext.GroupingStrategy.annotation;
		} else {
			throw new EnunciateException("Unknown grouping strategy: " + groupBy);
		}
	}

	@Override
	public boolean acceptType(Object type, MetadataAdapter metadata) {
		List<String> classAnnotations = metadata.getClassAnnotationNames(type);
		if (classAnnotations != null) {
			for (String classAnnotation : classAnnotations) {
				if ((Path.class.getName().equals(classAnnotation)) || (Provider.class.getName().equals(classAnnotation))
						|| (ApplicationPath.class.getName().equals(classAnnotation))) {
					return true;
				}
			}
		}
		return false;
	}

	public class MediaTypeDependencySpec implements DependencySpec {

		@Override
		public boolean accept(EnunciateModule module) {
			if (module instanceof MediaTypeDefinitionModule) {
				MediaTypeDefinitionModule definitionModule = (MediaTypeDefinitionModule) module;
				mediaTypeModules.add(definitionModule);

				// suggest to the media type definition module that it should
				// take a passive approach to detecting data types
				// because this module will be aggressively adding the data type
				// definitions to it.
				definitionModule.setDefaultDataTypeDetectionStrategy(DataTypeDetectionStrategy.passive);
				return true;
			}

			return false;
		}

		@Override
		public boolean isFulfilled() {
			// this spec is always fulfilled.
			return true;
		}

		@Override
		public String toString() {
			return "media type definition modules";
		}
	}
}
