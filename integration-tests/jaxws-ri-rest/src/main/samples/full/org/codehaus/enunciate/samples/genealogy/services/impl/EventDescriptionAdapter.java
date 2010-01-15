package org.codehaus.enunciate.samples.genealogy.services.impl;

import org.codehaus.enunciate.samples.genealogy.data.Event;
import org.codehaus.enunciate.samples.genealogy.data.EventDescriptionContainer;
import org.codehaus.enunciate.samples.genealogy.data.EventType;

import javax.xml.bind.annotation.adapters.XmlAdapter;
import java.util.Map;

/**
 * @author Ryan Heaton
 */
public class EventDescriptionAdapter extends XmlAdapter<EventDescriptionContainer, Map<EventType, String>> {

  @Override
  public Map<EventType, String> unmarshal(EventDescriptionContainer v) throws Exception {
    return null;
  }

  @Override
  public EventDescriptionContainer marshal(Map<EventType, String> v) throws Exception {
    return null;
  }
}
