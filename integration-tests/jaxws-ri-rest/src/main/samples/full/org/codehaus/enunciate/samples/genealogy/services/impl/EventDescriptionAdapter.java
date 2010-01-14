package org.codehaus.enunciate.samples.genealogy.services.impl;

import org.codehaus.enunciate.samples.genealogy.data.Event;
import org.codehaus.enunciate.samples.genealogy.data.EventType;

import javax.xml.bind.annotation.adapters.XmlAdapter;
import java.util.List;
import java.util.Map;

/**
 * @author Ryan Heaton
 */
public class EventDescriptionAdapter extends XmlAdapter<List<Event>, Map<EventType, String>> {

  @Override
  public Map<EventType, String> unmarshal(List<Event> v) throws Exception {
    return null;
  }

  @Override
  public List<Event> marshal(Map<EventType, String> v) throws Exception {
    return null;
  }
}
