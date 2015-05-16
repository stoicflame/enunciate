package com.webcohesion.enunciate.examples.genealogy.services.impl;

import com.webcohesion.enunciate.examples.genealogy.data.EventType;
import com.webcohesion.enunciate.examples.genealogy.data.EventDescriptionContainer;

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
