[#ftl]
[#--

    Copyright © 2006-2016 Web Cohesion (info@webcohesion.com)

    Licensed under the Apache License, Version 2.0 (the "License");
    you may not use this file except in compliance with the License.
    You may obtain a copy of the License at

        http://www.apache.org/licenses/LICENSE-2.0

    Unless required by applicable law or agreed to in writing, software
    distributed under the License is distributed on an "AS IS" BASIS,
    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
    See the License for the specific language governing permissions and
    limitations under the License.

--]
[#-- @ftlvariable name="resourceApis" type="java.util.List<com.webcohesion.enunciate.api.resources.ResourceApi>" --]
[#-- @ftlvariable name="serviceApis" type="java.util.List<com.webcohesion.enunciate.api.services.ServiceApi>" --]
[#-- @ftlvariable name="data" type="java.util.List<com.webcohesion.enunciate.api.datatype.Syntax>" --]
[#-- @ftlvariable name="downloads" type="java.util.List<com.webcohesion.enunciate.api.Download>" --]
[#-- @ftlvariable name="title" type="java.lang.String" --]
[#-- @ftlvariable name="indexPageName" type="java.lang.String" --]
[#-- @ftlvariable name="disableMountpoint" type="java.lang.Boolean" --]
[#-- @ftlvariable name="disableResourceLinks" type="java.lang.Boolean" --]
[#-- @ftlvariable name="apiRelativePath" type="java.lang.String" --]
[#-- @ftlvariable name="cssFile" type="java.lang.String" --]
[#-- @ftlvariable name="additionalCssFiles" type="java.util.List<java.lang.String>" --]
[#-- @ftlvariable name="copyright" type="java.lang.String" --]
[#-- @ftlvariable name="apiDoc" type="java.lang.String" --]
[#-- @ftlvariable name="swaggerUI" type="com.webcohesion.enunciate.api.InterfaceDescriptionFile" --]
[#-- @ftlvariable name="favicon" type="java.lang.String" --]
[#-- @ftlvariable name="includeApplicationPath" type="java.lang.Boolean" --]
[#--set up the subnavigation menus--]
[#assign nav_sections = { } /]
[#if resourceApis?size > 0]
  [#assign nav_sections = nav_sections + { "Resources" : "resources.html" }/]
[/#if]
[#if serviceApis?size > 0]
  [#assign nav_sections = nav_sections + { "Services" : "services.html" }/]
[/#if]
[#if data?size > 0]
  [#list data as syntax]
    [#assign nav_sections = { syntax.label : syntax.slug + ".html" } /]
  [/#list]
[/#if]
[#if downloads?size > 0]
  [#assign nav_sections = nav_sections + { "Files and Libraries" : "downloads.html"} /]
[/#if]
[#--Basic boilerplate macro.--]
[#macro boilerplate title=title breadcrumbs=[{"title" : "Home", "href" : indexPageName}] pagenav=[] codeblocks=true]
<!DOCTYPE html>
<html lang="en">
<head>
  <meta charset="utf-8">
  <meta http-equiv="X-UA-Compatible" content="IE=edge">
  <meta name="viewport" content="width=device-width, initial-scale=1">
  <!-- The above 3 meta tags *must* come first in the head; any other head content must come *after* these tags -->

  <title>${title}</title>

  <!-- Mobile viewport optimized: j.mp/bplateviewport -->
  <meta name="viewport" content="width=device-width, initial-scale=1.0">

  <!-- Bootstrap core CSS -->
  <link rel="stylesheet" href="https://maxcdn.bootstrapcdn.com/bootstrap/3.3.4/css/bootstrap.min.css">

  [#if cssFile??]
  <!--custom css for these pages-->
  <link rel="stylesheet" href="${cssFile}">
  [/#if]
  [#list additionalCssFiles as additionalCssFile]
  <link rel="stylesheet" href="${additionalCssFile}">
  [/#list]
  [#if favicon??]

  <link rel="shortcut icon" href="${favicon}" type="image/x-icon" />
  [/#if]

  <!-- HTML5 shim and Respond.js for IE8 support of HTML5 elements and media queries -->
  <!--[if lt IE 9]>
  <script src="https://oss.maxcdn.com/html5shiv/3.7.2/html5shiv.min.js"></script>
  <script src="https://oss.maxcdn.com/respond/1.4.2/respond.min.js"></script>
  <![endif]-->
</head>

<body data-spy="scroll" data-target="#apinav">

  <nav class="navbar navbar-inverse navbar-fixed-top">
    <div class="container-fluid">
      <div class="navbar-header">
        <button type="button" class="navbar-toggle collapsed" data-toggle="collapse" data-target="#navbar">
          <span class="sr-only">Toggle navigation</span>
          <span class="icon-bar"></span>
          <span class="icon-bar"></span>
          <span class="icon-bar"></span>
        </button>
        <a class="navbar-brand" href="${indexPageName}">${title}</a>
      </div>
      <div id="navbar" class="navbar-collapse collapse">
        <ul class="nav navbar-nav navbar-right">
  [#if serviceApis?size > 0]
          <li><a href="services.html">Services</a></li>
  [/#if]
  [#if resourceApis?size > 0]
          <li><a href="resources.html">Resources</a></li>
  [/#if]
  [#if data?size > 0]
          <li><a href="data.html">Data Types</a></li>
  [/#if]
  [#if downloads?size > 0]
          <li><a href="downloads.html">Files and Libraries</a></li>
  [/#if]
        </ul>
      </div>
    </div>
  </nav>

  <div class="container-fluid">
    <div class="row">
      <div class="col-sm-3 col-md-2 sidebar" id="apinav">
        <ul class="nav nav-sidebar">
  [#list pagenav as nav]
          <li><a href="${nav.href}"><abbr title="${nav.title}"><span class="sideoverflow">${nav.title}</span></abbr></a></li>
  [/#list]
  [#if pagenav?size > 0]
          <li class="divider"></li>
  [/#if]
          <li class="text-right"><a href="#top"><small>Back to Top</small></a></li>
        </ul>
      </div>

      <div class="col-sm-9 col-sm-offset-3 col-md-10 col-md-offset-2 main">
        <ol class="breadcrumb" id="top">
  [#list breadcrumbs as crumb]
          <li class="[#if crumb_has_next]active [/#if]dropdown"><a href="${crumb.href}">${crumb.title}</a></li>
  [/#list]
        </ol>

        [#nested/]

        <footer class="footer">
          <div class="container">
            <p class="text-muted">[#if copyright??]Copyright &copy; <script type="text/javascript" language="javascript">d = new Date;document.write(d.getFullYear());</script> <span xmlns:cc="http://creativecommons.org/ns#" property="cc:attributionName">${copyright}</span>. [/#if]Generated by <a href="http://enunciate.webcohesion.com">Enunciate</a>.</p>
          </div>
        </footer>

      </div>
    </div>
  </div>


  <!-- JavaScript placed at the end of the document so the pages load faster. -->
  <script src="https://cdnjs.cloudflare.com/ajax/libs/jquery/1.11.2/jquery.min.js"></script>

  <!-- Bootstrap core JavaScript
  ================================================== -->
  <script src="https://maxcdn.bootstrapcdn.com/bootstrap/3.3.4/js/bootstrap.min.js"></script>

  <!-- prettify code blocks. see http://code.google.com/p/google-code-prettify/ -->
  <script src="https://cdn.rawgit.com/google/code-prettify/master/loader/run_prettify.js" type="text/javascript"></script>
  <script>
    $(function() {
      $(".clickable-row").click(function() {
        window.document.location = $(this).data("href");
      });

      $('[data-toggle="tooltip"]').tooltip()
    });
  </script>

</body>
</html>
[/#macro]
[#--Macro that wraps its text in a deprecated <s> tag if the element is deprecated.--]
[#macro deprecation element]
  [#assign deprecated=(element?? && element.deprecated??)/]
  [#if deprecated]<s>[/#if][#nested/][#if deprecated]</s>[/#if]
[/#macro]
[@file name=indexPageName]
  [#assign pagenav=[]/]
  [#if resourceApis?size > 0]
    [#assign pagenav=pagenav + [{ "href" : "#resources", "title" : "Resources" }]/]
  [/#if]
  [#if serviceApis?size > 0]
    [#assign pagenav=pagenav + [{ "href" : "#services", "title" : "Services" }]/]
  [/#if]
  [#list data as syntax]
    [#assign pagenav=pagenav + [{ "href" : "#" + syntax.slug, "title" : syntax.label }]/]
  [/#list]
  [@boilerplate pagenav=pagenav]
    [#if apiDoc??]
  <div class="page-header">
    ${apiDoc}
  </div>
    [/#if]
    [#if resourceApis?size > 0]

  <h1 class="page-header" id="resources">Resources</h1>
      [#list resourceApis as resourceApi]
        [#if downloads?size > 0]

  <p>
    The resources use a data model that is supported by a set of client-side libraries that are made available on the
    <a href="downloads.html">files and libraries</a> page.
  </p>
        [/#if]
        [#if resourceApi.wadlFile??]

  <p>
    There is a <a href="${resourceApi.wadlFile.href}">WADL document</a> available that describes the resources API.
  </p>
        [/#if]
        [#if swaggerUI??]

  <p>
    You may also enjoy the <a href="${swaggerUI.href}">interactive interface</a> provided for this API by <a href="http://swagger.io">Swagger</a>.
  </p>
  <p>
    <a href="${swaggerUI.href}" class="btn btn-default">Try it out!</a>
  </p>
        [/#if]

  <table class="table table-hover resources">
    <thead>
    <tr>
        [#if resourceApi.includeResourceGroupName!false]
      <th align="center">name</th>
        [/#if]
      <th align="center">path</th>
      <th align="center">methods</th>
      <th align="center">description</th>
    </tr>
    </thead>
    <tbody data-link="row" class="rowlink">
        [#list resourceApi.resourceGroups as resourceGroup]
          [@processResourceGroup resourceGroup=resourceGroup/]
    <tr class="clickable-row" data-href="${resourceGroup.slug}.html">
        [#if resourceApi.includeResourceGroupName!false]
      <td>[@deprecation element=resourceGroup]<span class="resource-name">${resourceGroup.label}</span>[/@deprecation]</td>
        [/#if]
      <td class="text-nowrap"><ul class="list-unstyled">[#list resourceGroup.paths as path]<li><samp>[@deprecation element=resourceGroup]<span class="resource-path">[#if includeApplicationPath!false && resourceGroup.relativeContextPath?length > 0]/${resourceGroup.relativeContextPath}[/#if]${path.path}</span>[/@deprecation]</samp></li>[/#list]</ul></td>
      <td class="text-nowrap"><ul class="list-unstyled">[#list resourceGroup.paths as path]<li><samp>[@deprecation element=resourceGroup][#list path.methods as method]<span class="label label-default resource-method">${method}</span> [/#list][/@deprecation]</samp></li>[/#list]</ul></td>
      <td>[@deprecation element=resourceGroup]<span class="resource-description">${resourceGroup.description!"&nbsp;"}</span>[/@deprecation]</td>
    </tr>
        [/#list]
    </tbody>
  </table>
      [/#list]
    [/#if]
    [#if serviceApis?size > 0]

  <h1 class="page-header" id="services">Services</h1>
      [#list serviceApis as serviceApi]
        [#list serviceApi.serviceGroups as serviceGroup]

  <table class="table table-hover services">
    <caption>Namespace <code>${serviceGroup.namespace!"(Default)"}</code>[#if serviceGroup.wsdlFile??] (<a href="${serviceGroup.wsdlFile.href}">wsdl</a>)[/#if]</caption>
    <thead>
    <tr>
      <th align="center">name</th>
      <th align="center">description</th>
    </tr>
    </thead>
    <tbody data-link="row" class="rowlink">
          [#list serviceGroup.services as service]
            [@processService service=service/]
    <tr class="clickable-row" data-href="${service.slug}.html">
      <td>[@deprecation element=service]<span class="service-name[#list service.styles as style] ${style}[/#list]">${service.label}</span>[/@deprecation]</td>
      <td>[@deprecation element=service]<span class="service-description">${service.description!"&nbsp;"}</span>[/@deprecation]</td>
    </tr>
          [/#list]
    </tbody>
    </table>
        [/#list]
      [/#list]
      [#if downloads?size > 0]

  <p>The services API is also accessible by a set of client-side libraries that can be downloaded from the <a href="downloads.html">files and libraries page</a>.</p>
      [/#if]
    [/#if]
    [#if data?size > 0]

  <h1 class="page-header" id="data">Data Types</h1>
      [#list data as syntax]
        [@processDataSyntax syntax=syntax/]

  <h3 id="${syntax.slug}"><span class="syntax-name">${syntax.label}</span></h3>
        [#list syntax.namespaces as ns]
          [#if ns.types?size > 0]

  <table class="table table-hover datatypes">
            [#if ns.uri??]
              [#if ns.uri?length > 0]
    <caption>Namespace <code>${ns.uri}</code>[#if ns.schemaFile??] (<a href="${ns.schemaFile.href}">schema</a>)[/#if]</caption>
              [#else]
    <caption>Default Namespace [#if ns.schemaFile??] (<a href="${ns.schemaFile.href}">schema</a>)[/#if]</caption>
              [/#if]
            [/#if]
    <thead>
      <tr>
        <th align="center">type</th>
        <th align="center">description</th>
      </tr>
    </thead>
    <tbody data-link="row" class="rowlink">
            [#list ns.types as type]
    <tr class="clickable-row" data-href="${type.slug}.html">
      <td>[@deprecation element=type]<span class="datatype-name[#list type.styles as style] ${style}[/#list]">${type.label}</span>[/@deprecation]</td>
      <td>[@deprecation element=type]<span class="datatype-description">${type.description}</span>[/@deprecation]</td>
    </tr>
            [/#list]
    </tbody>
  </table>
          [/#if]
        [/#list]
      [/#list]
    [/#if]
  [/@boilerplate]
[/@file]
[@file name="data.html"]
  [#assign pagenav=[]/]
  [#list data as syntax]
    [#assign pagenav=pagenav + [{ "href" : "#" + syntax.slug, "title" : syntax.label }]/]
  [/#list]
  [@boilerplate title=title + ": Data Types" breadcrumbs=[{"title" : "Home", "href" : indexPageName}, {"title" : "Data Types" , "href" : "data.html"}] pagenav=pagenav]
  <h1 class="page-header" id="data">Data Types</h1>
    [#list data as syntax]

  <h3 id="${syntax.slug}">${syntax.label}</h3>
      [#list syntax.namespaces as ns]
        [#if ns.types?size > 0]

  <table class="table table-hover data-types">
          [#if ns.uri??]
            [#if ns.uri?length > 0]
  <caption>Namespace <code>${ns.uri}</code>[#if ns.schemaFile??] (<a href="${ns.schemaFile.href}">schema</a>)[/#if]</caption>
            [#else]
  <caption>Default Namespace [#if ns.schemaFile??] (<a href="${ns.schemaFile.href}">schema</a>)[/#if]</caption>
            [/#if]
          [/#if]
  <thead>
    <tr>
      <th align="center">type</th>
      <th align="center">description</th>
    </tr>
  </thead>
  <tbody data-link="row" class="rowlink">
          [#list ns.types as type]
    <tr class="clickable-row" data-href="${type.slug}.html">
      <td>[@deprecation element=type]<span class="datatype-name[#list type.styles as style] ${style}[/#list]">${type.label}</span>[/@deprecation]</td>
      <td>[@deprecation element=type]<span class="datatype-description">${type.description}</span>[/@deprecation]</td>
    </tr>
          [/#list]
  </tbody>
  </table>
        [/#if]
      [/#list]
    [/#list]
  [/@boilerplate]
[/@file]
[#if downloads?size > 0]
  [@file name="downloads.html"]
    [#assign pagenav=[]/]
    [#list downloads as download]
      [#assign pagenav=pagenav + [{ "href" : "#" + download.slug, "title" : download.name }]/]
    [/#list]
    [@boilerplate title=title + ": Files and Libraries" breadcrumbs=[{"title" : "Home", "href" : indexPageName}, { "title" : "Files and Libraries" , "href" : "downloads.html"}] codeblocks=true pagenav=pagenav]
  <h1 class="page-header">Files and Libraries</h1>

      [#list downloads as download]
  <h3 id="${download.slug}">${download.name}</h3>
        [#if download.created??]
  <p class="lead">Created ${download.created?date?string.long}</p>
        [/#if]
        [#if download.artifactId??]
  <dl class="dl-horizontal">
          [#if download.groupId??]
    <dt>groupId</dt>
    <dd>${download.groupId}</dd>
          [/#if]
          [#if download.artifactId??]
    <dt>artifactId</dt>
    <dd>${download.artifactId}</dd>
          [/#if]
          [#if download.version??]
    <dt>version</dt>
    <dd>${download.version}</dd>
  </dl>
          [/#if]
        [/#if]
        [#if download.description??]
  <p>${download.description}</p>
        [/#if]
  <table class="table table-hover downloadfiles">
    <caption>Files</caption>
    <thead>
    <tr>
      <th>name</th>
      <th>size</th>
      <th>description</th>
    </tr>
    </thead>
    <tbody data-link="row" class="rowlink">
        [#list download.files as file]
    <tr class="clickable-row" data-href="${file.name}">
      <td><span class="downloadfile-name">${file.name}</span></td>
      <td><span class="downloadfile-size">${file.size}</span></td>
      <td><span class="downloadfile-description">${file.description!download.description!"&nbsp;"}</span></td>
    </tr>
        [/#list]
    </tbody>
  </table>
      [/#list]
    [/@boilerplate]
  [/@file]
[/#if]
[#if resourceApis?size > 0]
  [@file name="resources.html"]
    [@boilerplate title=title + ": Resources" breadcrumbs=[{"title" : "Home", "href" : indexPageName}, {"title" : "Resources" , "href" : "resources.html"}]]
  <h1 class="page-header" id="resources">Resources</h1>

      [#list resourceApis as resourceApi]
        [#if downloads?size > 0]
  <p>
    The resources use a data model that is supported by a set of client-side libraries that are made available on the
    <a href="downloads.html">files and libraries</a> page.
  </p>
        [/#if]
        [#if resourceApi.wadlFile??]
  <p>
    There is a <a href="${resourceApi.wadlFile.href}">WADL document</a> available that describes the resources API.
  </p>
        [/#if]
        [#if swaggerUI??]
  <p>
    You may also enjoy the <a href="${swaggerUI.href}">interactive interface</a> provided for this API by <a href="http://swagger.io">Swagger</a>.
  </p>
  <p>
    <a href="${swaggerUI.href}" class="btn btn-default">Try it out!</a>
  </p>
        [/#if]
  <table class="table table-hover resources">
    <thead>
    <tr>
        [#if resourceApi.includeResourceGroupName!false]
      <th align="center">name</th>
        [/#if]
      <th align="center">path</th>
      <th align="center">methods</th>
      <th align="center">description</th>
    </tr>
    </thead>
    <tbody data-link="row" class="rowlink">
        [#list resourceApi.resourceGroups as resourceGroup]
    <tr class="clickable-row" data-href="${resourceGroup.slug}.html">
          [#if resourceApi.includeResourceGroupName!false]
      <td>[@deprecation element=resourceGroup]<span class="resource-name">${resourceGroup.label}</span>[/@deprecation]</td>
          [/#if]
      <td class="text-nowrap"><ul class="list-unstyled">[#list resourceGroup.paths as path]<li><samp>[@deprecation element=resourceGroup]<span class="resource-path">[#if includeApplicationPath!false && resourceGroup.relativeContextPath?length > 0]/${resourceGroup.relativeContextPath}[/#if]${path.path}</span>[/@deprecation]</samp></li>[/#list]</ul></td>
      <td class="text-nowrap"><ul class="list-unstyled">[#list resourceGroup.paths as path]<li><samp>[@deprecation element=resourceGroup][#list path.methods as method]<span class="label label-default resource-method">${method}</span> [/#list][/@deprecation]</samp></li>[/#list]</ul></td>
      <td>[@deprecation element=resourceGroup]<span class="resource-description">${resourceGroup.description!"&nbsp;"}</span>[/@deprecation]</td>
    </tr>
        [/#list]
    </tbody>
  </table>
      [/#list]
    [/@boilerplate]
  [/@file]
[/#if]
[#if serviceApis?size > 0]
  [@file name="services.html"]
    [@boilerplate title=title + ": Services" breadcrumbs=[{"title" : "Home", "href" : indexPageName}, {"title" : "Services" , "href" : "services.html"}]]
  <h1 class="page-header" id="services">Services</h1>
      [#list serviceApis as serviceApi]
        [#list serviceApi.serviceGroups as serviceGroup]

  <table class="table table-hover services">
  <caption>Namespace <code>${serviceGroup.namespace}</code>[#if serviceGroup.wsdlFile??] (<a href="${serviceGroup.wsdlFile.href}">wsdl</a>)[/#if]</caption>
  <thead>
    <tr>
      <th align="center">name</th>
      <th align="center">description</th>
    </tr>
  </thead>
  <tbody data-link="row" class="rowlink">
         [#list serviceGroup.services as service]
    <tr class="clickable-row" data-href="${service.slug}.html">
      <td>[@deprecation element=service]<span class="service-name[#list service.styles as style] ${style}[/#list]">${service.label}</span>[/@deprecation]</td>
      <td>[@deprecation element=service]<span class="service-description">${service.description!"&nbsp;"}</span>[/@deprecation]</td>
    </tr>
          [/#list]
  </tbody>
  </table>
        [/#list]
      [/#list]
      [#if downloads?size > 0]

  <p>The services API is also accessible by a set of client-side libraries that can be downloaded from the <a href="downloads.html">files and libraries page</a>.</p>
      [/#if]
    [/@boilerplate]
  [/@file]
[/#if]
[#macro processResourceGroup resourceGroup]
  [#assign pagenav=[]/]
  [#list resourceGroup.resources as resource]
    [#list resource.methods as method]
      [#assign path=resource.path/]
      [#if includeApplicationPath!false && resourceGroup.relativeContextPath?length > 0]
        [#assign path="/" + resourceGroup.relativeContextPath + resource.path/]
      [/#if]
      [#assign pagenav=pagenav + [{ "href" : "#" + method.slug, "title" : method.label + " " + path }]/]
    [/#list]
  [/#list]
  [#-- @ftlvariable name="resourceGroup" type="com.webcohesion.enunciate.api.resources.ResourceGroup" --]
  [@file name=resourceGroup.slug + ".html"]
    [@boilerplate title=title + ": " + resourceGroup.label breadcrumbs=[{"title" : "Home", "href" : indexPageName}, {"title" : "Resources" , "href" : "resources.html"}, {"title" : resourceGroup.label , "href" : resourceGroup.slug + ".html"}] pagenav=pagenav]
      <h1 class="page-header">${resourceGroup.label} <small>Resource</small></h1>
      [#if resourceGroup.description??]

      <p>${resourceGroup.description}</p>
      [/#if]
      [#list resourceGroup.resources as resource]
        [#if resource.since?? || resource.version?? || resource.seeAlso??]

      <dl class="dl-horizontal">
        [#if resource.since??]
        <dt>Available Since</dt>
        <dd>${resource.since}</dd>
        [/#if]
        [#if resource.version??]
        <dt>Version</dt>
        <dd>${resource.version}</dd>
        [/#if]
        [#if resource.seeAlso??]
          [#list resource.seeAlso as seeAlso]
        <dt>See Also</dt>
        <dd>${seeAlso}</dd>
          [/#list]
        [/#if]
      </dl>
        [/#if]
        [#list resource.methods as method]

      <div id="${method.slug}">
        <h3><span class="label label-default resource-method[#list method.styles as style] ${style}[/#list]">${method.label}</span> <span class="resource-path">[#if includeApplicationPath!false && resourceGroup.relativeContextPath?length > 0]/${resourceGroup.relativeContextPath}[/#if]${resource.path}[#if !disableResourceLinks!false] <a href="${apiRelativePath}[#if resourceGroup.relativeContextPath?length > 0]${resourceGroup.relativeContextPath}/[/#if]${resource.relativePath}" class="glyphicon glyphicon-new-window" target="_blank"></a>[/#if]</span></h3>
          [#if resourceGroup.deprecated?? || method.deprecated??]

        <div class="alert alert-danger">This method has been deprecated. [#if method.deprecated??] ${method.deprecated!""}[#else] ${resource.deprecated!""}[/#if]</div>
          [/#if]
          [#if method.description??]

        <p>${method.description}</p>
          [/#if]
          [#-- would be nice to enable a "Try it out" link to Swagger. See https://github.com/swagger-api/swagger-spec/issues/239
          [#if swaggerUI??]

        <p><a href="${swaggerUI.href}#!/${resourceGroup.label?url}/${method.slug}" class="btn btn-default">Try it out!</a></p>
          [/#if]
          --]
          [#assign securityRoles=method.securityRoles![]/]
          [#if (method.since?? || method.version?? || method.seeAlso?? || securityRoles?size > 0)]

        <dl class="dl-horizontal">
            [#if method.since??]
          <dt>Available Since</dt>
          <dd>${method.since}</dd>
            [/#if]
            [#if method.version??]
          <dt>Version</dt>
          <dd>${method.version}</dd>
            [/#if]
            [#if securityRoles?size > 0]
          <dt>Security Roles Allowed</dt>
          <dd>[#list securityRoles as role]${role}[#if role_has_next], [/#if][/#list]</dd>
            [/#if]
            [#if method.seeAlso??]
              [#list method.seeAlso as seeAlso]
          <dt>See Also</dt>
          <dd>${seeAlso}</dd>
              [/#list]
            [/#if]
        </dl>
          [/#if]
          [#if method.parameters?size > 0]

        <table class="table resource-parameters">
          <caption>Request Parameters</caption>
          <thead>
          <tr>
            <th>name</th>
            <th>type</th>
            <th>description</th>
            [#assign includeDefault=method.includeDefaultParameterValues/]
            [#if includeDefault]
            <th>default</th>
            [/#if]
            [#assign includeConstraints=method.hasParameterConstraints/]
            [#if includeConstraints]
            <th>constraints</th>
            [/#if]
            [#assign includeMultiplicity=method.hasParameterMultiplicity/]
            [#if includeMultiplicity]
            <th>multivalued</th>
            [/#if]
          </tr>
          </thead>
          <tbody>
            [#list method.parameters as parameter]
          <tr>
            <td><span class="parameter-name[#list parameter.styles as style] ${style}[/#list]">${parameter.name}</span></td>
            <td>${parameter.typeLabel}</td>
            <td><span class="parameter-description">${parameter.description!"&nbsp;"}</span></td>
              [#if includeDefault]
            <td><span class="parameter-default-value">${parameter.defaultValue!"&nbsp;"}</span></td>
              [/#if]
              [#if includeConstraints]
            <td><span class="parameter-constraints">${parameter.constraints!"&nbsp;"}</span></td>
              [/#if]
              [#if includeMultiplicity]
            <td><span class="parameter-multivalued">${parameter.multivalued?string("yes", "no")}</span></td>
              [/#if]
          </tr>
            [/#list]
          </tbody>
        </table>
          [/#if]
          [#if method.requestEntity??]

        <table class="table resource-request-body">
          <caption>Request Body</caption>
          <thead>
          <tr>
            <th>media type</th>
            <th>data type</th>
            [#if method.requestEntity.description??]
            <th>description</th>
            [/#if]
          </tr>
          </thead>
          <tbody>
            [#list method.requestEntity.mediaTypes as io]
          <tr>
            <td><abbr data-toggle="tooltip" data-placement="top" title="Use the &quot;Content-Type: ${io.mediaType}&quot; HTTP header to specify this media type to the server."><span class="request-type">${io.mediaType}</span></abbr></td>
            <td><span class="datatype-reference">[@referenceDataType referenceType=io.dataType!{"label" : "(custom)"}/][#if io.syntax??] (${io.syntax})[/#if]</span></td>
              [#if io_index = 0 && method.requestEntity.description??]
            <td[#if method.requestEntity.mediaTypes?size > 1] rowspan="${method.requestEntity.mediaTypes?size}" class="multi-row-description"[/#if]><span class="request-description">${method.requestEntity.description}</span></td>
              [/#if]
          </tr>
            [/#list]
          </tbody>
        </table>
          [/#if]
          [#if method.responseCodes?size > 0]

        <table class="table resource-response-codes">
          <caption>Response Codes</caption>
          <thead>
          <tr>
            <th>code</th>
            <th>condition</th>
            [#assign hasExpectedTypes=false/]
            [#list method.responseCodes as responseCode]
              [#if responseCode.mediaTypes?size > 0]
                [#assign hasExpectedTypes=true/]
              [/#if]
            [/#list]
            [#if hasExpectedTypes]
            <th>type</th>
            [/#if]
          </tr>
          </thead>
          <tbody>
            [#list method.responseCodes as responseCode]
          <tr>
            <td><span class="label label-[#if responseCode.code < 200]default[#elseif responseCode.code < 300]success[#elseif responseCode.code < 400]info[#elseif responseCode.code < 500]warning[#else]danger[/#if] response-code">${responseCode.code}</span></td>
            <td><span class="response-condition">${responseCode.condition!""}</span></td>
              [#if hasExpectedTypes]
            <td><ul class="list-unstyled">[#list responseCode.mediaTypes as io]<li><span class="datatype-reference">[@referenceDataType referenceType=io.dataType!{"label" : "(custom)"}/][#if io.syntax??] (${io.syntax})[/#if]</span></li>[/#list]</ul></td>
              [/#if]
          </tr>
            [/#list]
          </tbody>
        </table>
          [/#if]
          [#if method.responseEntity??]

        <table class="table resource-response-body">
          <caption>Response Body</caption>
          <thead>
          <tr>
            <th>media type</th>
            <th>data type</th>
            [#if method.responseEntity.description??]
            <th>description</th>
            [/#if]
          </tr>
          </thead>
          <tbody>
            [#list method.responseEntity.mediaTypes as io]
          <tr>
            <td><abbr data-toggle="tooltip" data-placement="top" title="Use the &quot;Accept: ${io.mediaType}&quot; HTTP header to request that this media type be provided by the server."><span class="response-type">${io.mediaType}</span></abbr></td>
            <td><span class="datatype-reference">[@referenceDataType referenceType=io.dataType!{"label" : "(custom)"}/][#if io.syntax??] (${io.syntax})[/#if]</span></td>
              [#if io_index = 0 && method.responseEntity.description??]
            <td[#if method.responseEntity.mediaTypes?size > 1] rowspan="${method.responseEntity.mediaTypes?size}" class="multi-row-description"[/#if]><span class="response-description">${method.responseEntity.description}</span></td>
              [/#if]
          </tr>
            [/#list]
          </tbody>
        </table>
          [/#if]
          [#if method.warnings?size > 0]

        <table class="table resource-response-warnings">
          <caption>Response Warnings</caption>
          <thead>
          <tr>
            <th>code</th>
            <th>condition</th>
          </tr>
          </thead>
          <tbody>
            [#list method.warnings as responseCode]
          <tr>
            <td><span class="label label-default warning-code">${responseCode.code}</span></td>
            <td><span class="warning-condition">${responseCode.condition!""}</span></td>
          </tr>
            [/#list]
          </tbody>
        </table>
          [/#if]
          [#if method.responseHeaders?size > 0]

        <table class="table resource-response-headers">
          <caption>Response Headers</caption>
          <thead>
          <tr>
            <th>name</th>
            <th>description</th>
          </tr>
          </thead>
          <tbody>
            [#list method.responseHeaders as header]
          <tr>
            <td><span class="header-name">${header.name}</span></td>
            <td><span class="header-description">${header.description!"&nbsp;"}</span></td>
          </tr>
            [/#list]
          </tbody>
        </table>

          [/#if]
          [#if method.example??]
        <h4>Example</h4>

        <div class="container-fluid example panel">
          <div class="row panel-body">
            <div class="col-md-6">
              <h5>Request</h5>
              <pre>
${method.example.requestHeaders?xhtml}
            [#if method.example.requestLang??]
                <code class="prettyprint language-${method.example.requestLang}">
${method.example.requestBody?xhtml}
                </code>
            [/#if]
              </pre>
            </div>
            <div class="col-md-6">
              <h5>Response</h5>
              <pre>
${method.example.responseHeaders?xhtml}
            [#if method.example.responseLang??]
                <code class="prettyprint language-${method.example.responseLang}">
${method.example.responseBody?xhtml}
                </code>
            [/#if]
              </pre>
            </div>
          </div>
        </div>
          [/#if]
      </div>
        [/#list]
      [/#list]
    [/@boilerplate]
  [/@file]
[/#macro]
[#macro processService service]
  [#assign pagenav=[]/]
  [#list service.operations as operation]
    [#assign pagenav=pagenav + [{ "href" : "#" + operation.slug, "title" : operation.name }]/]
  [/#list]
  [#-- @ftlvariable name="service" type="com.webcohesion.enunciate.api.services.Service" --]
  [@file name=service.slug + ".html"]
    [@boilerplate title=title + ": " + service.label breadcrumbs=[{"title" : "Home", "href" : indexPageName}, {"title" : service.label , "href" : service.slug + ".html"}] pagenav=pagenav]
      <h1 class="page-header">${service.label} <small>Service</small></h1>
      [#if service.deprecated??]

      <div class="alert alert-danger">This service has been deprecated. ${service.deprecated}</div>
      [/#if]
      [#if service.description??]

      <p>${service.description}</p>
      [/#if]

      <dl class="dl-horizontal">
        [#if service.namespace?? && service.namespace?length > 0]
        <dt>Namespace</dt>
        <dd>${service.namespace}</dd>
        [/#if]
        [#if service.group.wsdlFile??]
        <dt>WSDL</dt>
        <dd><a href="${service.group.wsdlFile.href}">${service.group.wsdlFile.href}</a></dd>
        [/#if]
        [#if service.path??]
        <dt>Path</dt>
        <dd><a href="${apiRelativePath}${service.path?substring(1)}">${service.path}</a></dd>
        [/#if]
        [#if service.since??]
        <dt>Available Since</dt>
        <dd>${service.since}</dd>
        [/#if]
        [#if service.version??]
        <dt>Version</dt>
        <dd>${service.version}</dd>
        [/#if]
        [#if service.seeAlso??]
          [#list service.seeAlso as seeAlso]
        <dt>See Also</dt>
        <dd>${seeAlso}</dd>
          [/#list]
        [/#if]
      </dl>
      [#list service.operations as operation]

      <h2 id="${operation.slug}">${operation.name} <small>Operation</small></h2>
        [#if operation.deprecated??]

      <div class="alert alert-danger">This method has been deprecated. ${operation.deprecated}</div>
        [/#if]
        [#if operation.description??]

        <p>${operation.description}</p>
        [/#if]
        [#if (operation.since?? || operation.version?? || operation.seeAlso??)]

      <dl class="dl-horizontal">
          [#if operation.since??]
        <dt>Available Since</dt>
        <dd>${operation.since}</dd>
          [/#if]
          [#if operation.version??]
        <dt>Version</dt>
        <dd>${operation.version}</dd>
          [/#if]
          [#if operation.seeAlso??]
            [#list operation.seeAlso as seeAlso]
        <dt>See Also</dt>
        <dd>${seeAlso}</dd>
            [/#list]
          [/#if]
      </dl>
        [/#if]
        [#if operation.inputParameters?size > 0]

      <table class="table service-input-parameters">
        <caption>Input Parameters</caption>
        <thead>
        <tr>
          <th>name</th>
          <th>type</th>
          <th>description</th>
        </tr>
        </thead>
        <tbody>
          [#list operation.inputParameters as parameter]
        <tr>
          <td><span class="parameter-name[#list parameter.styles as style] ${style}[/#list]">${parameter.name}</span></td>
          <td><span class="datatype-reference">[@referenceDataType referenceType=parameter.dataType/]</span></td>
          <td><span class="parameter-description">${parameter.description!"&nbsp;"}</span></td>
        </tr>
          [/#list]
        </tbody>
      </table>
        [/#if]
        [#if operation.httpRequestHeaders?size > 0]

      <table class="table service-input-parameters">
        <caption>HTTP Request Headers</caption>
        <thead>
        <tr>
          <th>name</th>
          <th>type</th>
          <th>description</th>
        </tr>
        </thead>
        <tbody>
          [#list operation.httpRequestHeaders as parameter]
        <tr>
          <td><span class="parameter-name[#list parameter.styles as style] ${style}[/#list]">${parameter.name}</span></td>
          <td><span class="datatype-reference">[@referenceDataType referenceType=parameter.dataType/]</span></td>
          <td><span class="parameter-description">${parameter.description!"&nbsp;"}</span></td>
        </tr>
          [/#list]
        </tbody>
      </table>
        [/#if]
        [#if operation.outputParameters?size > 0]

      <table class="table service-output-parameters">
        <caption>Output Parameters</caption>
        <thead>
        <tr>
          <th>name</th>
          <th>type</th>
          <th>description</th>
        </tr>
        </thead>
        <tbody>
          [#list operation.outputParameters as parameter]
        <tr>
          <td><span class="parameter-name[#list parameter.styles as style] ${style}[/#list]">${parameter.name}</span></td>
          <td><span class="datatype-reference">[@referenceDataType referenceType=parameter.dataType/]</span></td>
          <td><span class="parameter-description">${parameter.description!"&nbsp;"}</span></td>
        </tr>
          [/#list]
        </tbody>
      </table>
        [/#if]
        [#if operation.returnType??]

      <table class="table service-return-value">
        <caption>Return Value</caption>
        <thead>
        <tr>
          <th>type</th>
          <th>description</th>
        </tr>
        </thead>
        <tbody>
        <tr>
          <td>[@referenceDataType referenceType=operation.returnType/]</td>
          <td><span class="return-description">${operation.returnDescription!"&nbsp;"}</span></td>
        </tr>
        </tbody>
      </table>
        [/#if]
        [#if operation.faults?size > 0]

      <table class="table service-faults">
        <caption>Faults</caption>
        <thead>
        <tr>
          <th>name</th>
          <th>type</th>
          <th>conditions</th>
        </tr>
        </thead>
        <tbody>
          [#list operation.faults as fault]
        <tr>
          <td><span class="fault-name[#list fault.styles as style] ${style}[/#list]">${fault.name}</span></td>
          <td><span class="datatype-reference">[@referenceDataType referenceType=fault.dataType/]</span></td>
          <td><span class="fault-conditions">${fault.conditions!"&nbsp;"}</span></td>
        </tr>
          [/#list]
        </tbody>
      </table>
        [/#if]
      [/#list]
    [/@boilerplate]
  [/@file]
[/#macro]
[#macro processDataSyntax syntax]
  [#-- @ftlvariable name="syntax" type="com.webcohesion.enunciate.api.datatype.Syntax" --]
  [@file name=syntax.slug + ".html"]
    [@boilerplate title=title + ": " + syntax.label breadcrumbs=[{"title" : "Home", "href" : indexPageName}, {"title" : syntax.label , "href" : syntax.slug + ".html"} ]]
  <h1 class="page-header">${syntax.label}</h1>
      [#list syntax.namespaces as ns]
        [#if ns.types?size > 0]

  <table class="table table-hover datatypes">
          [#if ns.uri??]
            [#if ns.uri?length > 0]
  <caption>Namespace <code>${ns.uri}</code>[#if ns.schemaFile??] (<a href="${ns.schemaFile.href}">schema</a>)[/#if]</caption>
            [#else]
  <caption>Default Namespace [#if ns.schemaFile??] (<a href="${ns.schemaFile.href}">schema</a>)[/#if]</caption>
            [/#if]
          [/#if]
  <thead>
    <tr>
      <th align="center">type</th>
      <th align="center">description</th>
    </tr>
  </thead>
  <tbody data-link="row" class="rowlink">
          [#list ns.types as type]
            [@processDataType type=type/]
    <tr class="clickable-row" data-href="${type.slug}.html">
      <td>[@deprecation element=type]<span class="datatype-name[#list type.styles as style] ${style}[/#list]">${type.label}</span>[/@deprecation]</td>
      <td>[@deprecation element=type]<span class="datatype-description">${type.description}</span>[/@deprecation]</td>
    </tr>
          [/#list]
  </tbody>
  </table>
        [/#if]
      [/#list]
    [/@boilerplate]
  [/@file]
[/#macro]
[#macro processDataType type]
  [#-- @ftlvariable name="type" type="com.webcohesion.enunciate.api.datatype.DataType" --]
  [#assign pagenav=[]/]
  [#if type.values??]
    [#list type.values as value]
      [#assign pagenav=pagenav + [{ "href" : "#" + value.value, "title" : value.value }]/]
    [/#list]
  [/#if]
  [@file name=type.slug + ".html"]
    [@boilerplate title=title + ": " + type.label breadcrumbs=[{"title" : "Home", "href" : indexPageName}, {"title" : type.syntax.label , "href" : type.syntax.slug + ".html"}, {"title" : type.label , "href" : type.slug + ".html"} ] pagenav=pagenav codeblocks=true]
      <h1 class="page-header">${type.label} <small>Data Type</small></h1>
      [#if type.deprecated??]

      <div class="alert alert-danger">This data type has been deprecated. ${type.deprecated}</div>
      [/#if]
      [#if type.description??]

      <p>${type.description}</p>
      [/#if]

      <dl class="dl-horizontal">
      [#if type.namespace.uri??]
        <dt>Namespace</dt>
        [#if type.namespace.uri?length > 0]
        <dd><code>${type.namespace.uri}</code></dd>
        [#else]
        <dd>(Default)</dd>
        [/#if]
      [/#if]
      [#if type.namespace.schemaFile??]
        <dt>Schema</dt>
        <dd><a href="${type.namespace.schemaFile.href}">${type.namespace.schemaFile.href}</a></dd>
      [/#if]
      [#if type.since??]
        <dt>Available Since</dt>
        <dd>${type.since}</dd>
      [/#if]
      [#if type.version??]
        <dt>Version</dt>
        <dd>${type.version}</dd>
      [/#if]
      [#if type.abstract]
        <dt>Abstract Type</dt>
        <dd></dd>
      [/#if]
      [#if type.subtypes??]
        <dt>Subtypes</dt>
        <dd>[#list type.subtypes as subtype][#if subtype.slug??]<a href="${subtype.slug}.html">${subtype.label}</a>[#else]${subtype.label}[/#if][#if subtype_has_next], [/#if][/#list]</dd>
      [/#if]
      [#if type.interfaces??]
        <dt>Implemented Interfaces</dt>
        <dd>[#list type.interfaces as iface]<a href="${iface.slug}.html">${iface.label}</a>[#if iface_has_next], [/#if][/#list]</dd>
      [/#if]
      [#if type.seeAlso??]
        [#list type.seeAlso as seeAlso]
        <dt>See Also</dt>
        <dd>${seeAlso}</dd>
        [/#list]
      [/#if]
      </dl>
      [#if type.values??]

      <table class="table datatype-values">
        <caption>Values</caption>
        <thead>
        <tr>
          <th>value</th>
          <th>description</th>
        </tr>
        </thead>
        <tbody>
          [#list type.values as value]
        <tr>
          <td><span class="value-value[#list value.styles as style] ${style}[/#list]" id="${value.value}">${value.value}</span></td>
          <td><span class="value-description">[#if value.since??]<span class="label label-default">since ${value.since}</span> [/#if]${value.description!"&nbsp;"}</span></td>
        </tr>
          [/#list]
        </tbody>
      </table>
      [/#if]
      [#if type.properties??]

      <table class="table datatype-properties">
        <caption>Properties</caption>
        <thead>
        <tr>
          <th>name</th>
          <th>data type</th>
        [#list type.propertyMetadata?keys as meta]
          <th>${type.propertyMetadata[meta]}</th>
        [/#list]
          <th>description</th>
        </tr>
        </thead>
        <tbody>
          [#list type.properties as property]
        <tr>
          <td>[@deprecation element=property]<span id="prop-${property.name}" class="property-name[#list property.styles as style] ${style}[/#list]">${property.name}</span>[/@deprecation]</td>
          <td>[@deprecation element=property]<span class="datatype-reference">[@referenceDataType referenceType=property.dataType/]</span>[/@deprecation]</td>
            [#list type.propertyMetadata?keys as meta]
          <td>[@deprecation element=property]<span class="property-${meta}">[@printPropertyMetadata property=property meta=meta/]</span>[/@deprecation]</td>
            [/#list]
          <td>[@deprecation element=property]<span class="property-description">[#if property.since??]<span class="label label-default">since ${property.since}</span> [/#if]${property.description!"&nbsp;"}</span>[/@deprecation]</td>
        </tr>
          [/#list]
        </tbody>
          [#if type.supertypes??]
            [#list type.supertypes as supertype]
              [#if supertype.value?? && supertype.value.properties?? && supertype.value.properties?size > 0]
        <tr>
          <td colspan="${3 + type.propertyMetadata?size}"><h5 class="text-muted">Properties inherited from <a href="${supertype.slug}.html">${supertype.label}</a></h5></td>
        </tr>
        <tbody>
                [#list supertype.value.properties as superProperty]
        <tr>
          <td><span class="property-name[#list superProperty.styles as style] ${style}[/#list]">${superProperty.name}</span></td>
          <td><span class="datatype-reference">[@referenceDataType referenceType=superProperty.dataType/]</span></td>
            [#list type.propertyMetadata?keys as meta]
          <td><span class="property-${meta}">[@printPropertyMetadata property=superProperty meta=meta/]</span></td>
            [/#list]
          <td><span class="property-description">[#if superProperty.since??]<span class="label label-default">since ${superProperty.since}</span> [/#if]${superProperty.description!"&nbsp;"}</span></td>
        </tr>
                [/#list]
        </tbody>
              [/#if]
            [/#list]
          [/#if]
      </table>
      [/#if]
      [#if type.example??]

      <p class="lead">Example</p>
        [#if type.abstract && type.subtypes?? ]

      <div class="alert alert-warning">This data type is abstract. The example below may be incomplete. More accurate examples can be found in subtypes pages.</div>
        [/#if]
      
      <pre class="prettyprint language-${type.example.lang} example">${type.example.body?xhtml}</pre>
      [/#if]
    [/@boilerplate]
  [/@file]
[/#macro]
[#macro referenceDataType referenceType]
[#-- @ftlvariable name="type" type="com.webcohesion.enunciate.api.datatype.DataTypeReference" --]
[#if referenceType.containers??][#list referenceType.containers as container]${container?string} of [/#list][/#if][#if referenceType.slug??]<a href="${referenceType.slug}.html">[/#if]${referenceType.label!"(custom)"}[#if referenceType.slug??]</a>[/#if]
[/#macro]
[#macro printPropertyMetadata property meta]
  [#assign metaValue=property[meta]!({ "structure" : true })/]
  [#if metaValue?is_hash && metaValue.structure!false]
[#if metaValue.href??]<a href="${metaValue.href}">[/#if][#if metaValue.title??]<abbr title="${metaValue.title}">[/#if]${metaValue.value!"&nbsp;"}[#if metaValue.title??]</abbr>[/#if][#if metaValue.href??]</a>[/#if]
  [#else]
${metaValue}
  [/#if]
[/#macro]
