package org.codehaus.enunciate.samples.genealogy.data;

import com.sun.xml.bind.AnyTypeAdapter;

import javax.xml.bind.annotation.XmlSeeAlso;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;

/**
 * @author Ryan Heaton
 */
@XmlJavaTypeAdapter (AnyTypeAdapter.class)
@XmlSeeAlso(ContributorImpl.class)
public interface Contributor {

  String getName();

}
