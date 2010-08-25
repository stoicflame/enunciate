/*
 * Copyright 2006-2008 Web Cohesion
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.codehaus.enunciate.modules.jaxws_client;

import junit.framework.TestCase;
import org.codehaus.enunciate.examples.jaxws_client.schema.*;
import org.codehaus.enunciate.examples.jaxws_client.schema.animals.Cat;
import org.codehaus.enunciate.examples.jaxws_client.schema.draw.Canvas;
import org.codehaus.enunciate.examples.jaxws_client.schema.draw.CanvasAttachment;
import org.codehaus.enunciate.examples.jaxws_client.schema.structures.House;
import org.codehaus.enunciate.examples.jaxws_client.schema.vehicles.Bus;
import org.codehaus.jackson.jaxrs.JacksonJaxbJsonProvider;
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.node.ObjectNode;
import org.joda.time.DateTime;

import javax.mail.util.ByteArrayDataSource;
import javax.ws.rs.core.MediaType;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.util.*;

/**
 * @author Ryan Heaton
 */
public class TestGeneratedJsonTypeSerialization extends TestCase {

  /**
   * tests the basic shapes.
   */
  public void testBasicShapes() throws Exception {
    Circle circle = new Circle();
    circle.setColor(Color.BLUE);
    circle.setId("someid");
    circle.setLineStyle(LineStyle.solid);
    circle.setPositionX(8);
    circle.setPositionY(9);
    circle.setRadius(10);

    JacksonJaxbJsonProvider provider = new JacksonJaxbJsonProvider();
    ObjectMapper circleMapper = provider.locateMapper(Circle.class, MediaType.APPLICATION_JSON_TYPE);
    ObjectMapper clientMapper = new ObjectMapper();

    ByteArrayOutputStream out = new ByteArrayOutputStream();
    circleMapper.writeValue(out, circle);
    ByteArrayInputStream in = new ByteArrayInputStream(out.toByteArray());

    shapes.json.Circle clientCircle = clientMapper.readValue(in, shapes.json.Circle.class);
    assertSame(shapes.json.Color.BLUE, clientCircle.getColor());
    assertEquals("someid", clientCircle.getId());
    assertEquals(shapes.json.LineStyle.solid, clientCircle.getLineStyle());
    assertEquals(8, clientCircle.getPositionX());
    assertEquals(9, clientCircle.getPositionY());
    assertEquals(10, clientCircle.getRadius());

    out = new ByteArrayOutputStream();
    clientMapper.writeValue(out, clientCircle);
    in = new ByteArrayInputStream(out.toByteArray());

    circle = circleMapper.readValue(in, Circle.class);
    assertSame(Color.BLUE, circle.getColor());
    assertEquals("someid", circle.getId());
    assertEquals(LineStyle.solid, circle.getLineStyle());
    assertEquals(8, circle.getPositionX());
    assertEquals(9, circle.getPositionY());
    assertEquals(10, circle.getRadius());

    ObjectMapper rectangleMapper = provider.locateMapper(Rectangle.class, MediaType.APPLICATION_JSON_TYPE);

    Rectangle rectangle = new Rectangle();
    rectangle.setColor(Color.GREEN);
    rectangle.setId("rectid");
    rectangle.setHeight(500);
    rectangle.setWidth(1000);
    rectangle.setLineStyle(LineStyle.dotted);
    rectangle.setPositionX(-100);
    rectangle.setPositionY(-300);

    out = new ByteArrayOutputStream();
    rectangleMapper.writeValue(out, rectangle);
    in = new ByteArrayInputStream(out.toByteArray());

    shapes.json.Rectangle clientRect = clientMapper.readValue(in, shapes.json.Rectangle.class);
    assertSame(shapes.json.Color.GREEN, clientRect.getColor());
    assertEquals("rectid", clientRect.getId());
    assertEquals(shapes.json.LineStyle.dotted, clientRect.getLineStyle());
    assertEquals(500, clientRect.getHeight());
    assertEquals(1000, clientRect.getWidth());
    assertEquals(-100, clientRect.getPositionX());
    assertEquals(-300, clientRect.getPositionY());

    out = new ByteArrayOutputStream();
    clientMapper.writeValue(out, clientRect);
    in = new ByteArrayInputStream(out.toByteArray());

    rectangle = rectangleMapper.readValue(in, Rectangle.class);
    assertSame(Color.GREEN, rectangle.getColor());
    assertEquals("rectid", rectangle.getId());
    assertEquals(LineStyle.dotted, rectangle.getLineStyle());
    assertEquals(500, rectangle.getHeight());
    assertEquals(1000, rectangle.getWidth());
    assertEquals(-100, rectangle.getPositionX());
    assertEquals(-300, rectangle.getPositionY());

    ObjectMapper triangleMapper = provider.locateMapper(Triangle.class, MediaType.APPLICATION_JSON_TYPE);

    Triangle triangle = new Triangle();
    triangle.setBase(90);
    triangle.setColor(Color.RED);
    triangle.setHeight(100);
    triangle.setId("triangleId");
    triangle.setLineStyle(LineStyle.dashed);
    triangle.setPositionX(0);
    triangle.setPositionY(-10);

    out = new ByteArrayOutputStream();
    triangleMapper.writeValue(out, triangle);
    in = new ByteArrayInputStream(out.toByteArray());

    shapes.json.Triangle clientTri = clientMapper.readValue(in, shapes.json.Triangle.class);
    assertSame(shapes.json.Color.RED, clientTri.getColor());
    assertEquals("triangleId", clientTri.getId());
    assertEquals(shapes.json.LineStyle.dashed, clientTri.getLineStyle());
    assertEquals(90, clientTri.getBase());
    assertEquals(100, clientTri.getHeight());
    assertEquals(0, clientTri.getPositionX());
    assertEquals(-10, clientTri.getPositionY());

    out = new ByteArrayOutputStream();
    clientMapper.writeValue(out, clientTri);
    in = new ByteArrayInputStream(out.toByteArray());

    triangle = triangleMapper.readValue(in, Triangle.class);
    assertSame(Color.RED, triangle.getColor());
    assertEquals(90, triangle.getBase());
    assertEquals(100, triangle.getHeight());
    assertEquals("triangleId", triangle.getId());
    assertSame(LineStyle.dashed, triangle.getLineStyle());
    assertEquals(0, triangle.getPositionX());
    assertEquals(-10, triangle.getPositionY());
  }

  /**
   * tests bus.  This one has an element wrapper.
   */
  public void testBus() throws Exception {
    Bus bus = new Bus();
    bus.setId("some bus");
    Label cityBus = new Label();
    cityBus.setValue("city");
    Label countryBus = new Label();
    countryBus.setValue("country");
    Label longDistanceBus = new Label();
    longDistanceBus.setValue("long-distance");

    bus.setId("bus id");
    bus.setLabels(Arrays.asList(cityBus, countryBus, longDistanceBus));
    Rectangle door = new Rectangle();
    door.setColor(Color.BLUE);
    door.setWidth(2);
    door.setHeight(4);
    door.setLineStyle(LineStyle.solid);
    bus.setDoor(door);
    Rectangle frame = new Rectangle();
    frame.setHeight(10);
    frame.setWidth(50);
    frame.setColor(Color.YELLOW);
    frame.setLineStyle(LineStyle.solid);
    bus.setFrame(frame);
    Circle front = new Circle();
    front.setColor(Color.BLUE);
    front.setLineStyle(LineStyle.dotted);
    front.setRadius(6);
    Circle back = new Circle();
    back.setColor(Color.BLUE);
    back.setLineStyle(LineStyle.dotted);
    back.setRadius(7);
    bus.setWheels(new Circle[] {front, back});
    Rectangle window1 = new Rectangle();
    window1.setColor(Color.BLUE);
    window1.setWidth(2);
    window1.setHeight(2);
    window1.setLineStyle(LineStyle.solid);
    Rectangle window2 = new Rectangle();
    window2.setColor(Color.BLUE);
    window2.setWidth(2);
    window2.setHeight(2);
    window2.setLineStyle(LineStyle.solid);
    Rectangle window3 = new Rectangle();
    window3.setColor(Color.BLUE);
    window3.setWidth(2);
    window3.setHeight(2);
    window3.setLineStyle(LineStyle.solid);
    bus.setWindows(Arrays.asList(window1, window2, window3));
    Map<Integer, Circle> riders = new HashMap<Integer, Circle>();
    Circle rider3 = new Circle();
    rider3.setRadius(3);
    riders.put(3, rider3);
    Circle rider4 = new Circle();
    rider4.setRadius(4);
    riders.put(4, rider4);

    JacksonJaxbJsonProvider provider = new JacksonJaxbJsonProvider();
    ObjectMapper busMapper = provider.locateMapper(Bus.class, MediaType.APPLICATION_JSON_TYPE);
    ObjectMapper clientMapper = new ObjectMapper();

    ByteArrayOutputStream out = new ByteArrayOutputStream();
    busMapper.writeValue(out, bus);
    ByteArrayInputStream in = new ByteArrayInputStream(out.toByteArray());
    shapes.json.vehicles.Bus clientBus = clientMapper.readValue(in, shapes.json.vehicles.Bus.class);
    assertEquals("bus id", clientBus.getId());
    ArrayList<String> labels = new ArrayList<String>(Arrays.asList("city", "country", "long-distance"));
    for (Object l : clientBus.getLabels()) {
      shapes.json.Label label = (shapes.json.Label) l;
      assertTrue(labels.remove(label.getValue()));
    }
    shapes.json.Rectangle clientDoor = clientBus.getDoor();
    assertSame(shapes.json.Color.BLUE, clientDoor.getColor());
    assertEquals(2, clientDoor.getWidth());
    assertEquals(4, clientDoor.getHeight());
    assertSame(shapes.json.LineStyle.solid, clientDoor.getLineStyle());
    shapes.json.Rectangle clientFrame = clientBus.getFrame();
    assertEquals(10, clientFrame.getHeight());
    assertEquals(50, clientFrame.getWidth());
    assertSame(shapes.json.Color.YELLOW, clientFrame.getColor());
    assertSame(shapes.json.LineStyle.solid, clientFrame.getLineStyle());
    shapes.json.Circle[] clientWheels = clientBus.getWheels();
    assertEquals(2, clientWheels.length);
    assertEquals(6, clientWheels[0].getRadius());
    assertSame(shapes.json.Color.BLUE, clientWheels[0].getColor());
    assertSame(shapes.json.LineStyle.dotted, clientWheels[0].getLineStyle());
    assertEquals(7, clientWheels[1].getRadius());
    assertSame(shapes.json.Color.BLUE, clientWheels[1].getColor());
    assertSame(shapes.json.LineStyle.dotted, clientWheels[1].getLineStyle());
    shapes.json.Rectangle[] clientWindows = (shapes.json.Rectangle[]) clientBus.getWindows().toArray(new shapes.json.Rectangle[3]);
    assertEquals(2, clientWindows[0].getWidth());
    assertEquals(2, clientWindows[0].getHeight());
    assertEquals(shapes.json.Color.BLUE, clientWindows[0].getColor());
    assertEquals(shapes.json.LineStyle.solid, clientWindows[0].getLineStyle());
    assertEquals(2, clientWindows[1].getWidth());
    assertEquals(2, clientWindows[1].getHeight());
    assertEquals(shapes.json.Color.BLUE, clientWindows[1].getColor());
    assertEquals(shapes.json.LineStyle.solid, clientWindows[1].getLineStyle());
    assertEquals(2, clientWindows[2].getWidth());
    assertEquals(2, clientWindows[2].getHeight());
    assertEquals(shapes.json.Color.BLUE, clientWindows[2].getColor());
    assertEquals(shapes.json.LineStyle.solid, clientWindows[2].getLineStyle());

    out = new ByteArrayOutputStream();
    clientMapper.writeValue(out, clientBus);
    bus = busMapper.readValue(new ByteArrayInputStream(out.toByteArray()), Bus.class);
    door = bus.getDoor();
    assertSame(Color.BLUE, door.getColor());
    assertEquals(2, door.getWidth());
    assertEquals(4, door.getHeight());
    assertSame(LineStyle.solid, door.getLineStyle());
    frame = bus.getFrame();
    assertEquals(10, frame.getHeight());
    assertEquals(50, frame.getWidth());
    assertSame(Color.YELLOW, frame.getColor());
    assertSame(LineStyle.solid, frame.getLineStyle());
    Circle[] wheels = bus.getWheels();
    assertEquals(2, wheels.length);
    assertEquals(6, wheels[0].getRadius());
    assertSame(Color.BLUE, wheels[0].getColor());
    assertSame(LineStyle.dotted, wheels[0].getLineStyle());
    assertEquals(7, wheels[1].getRadius());
    assertSame(Color.BLUE, wheels[1].getColor());
    assertSame(LineStyle.dotted, wheels[1].getLineStyle());
    Rectangle[] windows = bus.getWindows().toArray(new Rectangle[3]);
    assertEquals(2, windows[0].getWidth());
    assertEquals(2, windows[0].getHeight());
    assertEquals(Color.BLUE, windows[0].getColor());
    assertEquals(LineStyle.solid, windows[0].getLineStyle());
    assertEquals(2, windows[1].getWidth());
    assertEquals(2, windows[1].getHeight());
    assertEquals(Color.BLUE, windows[1].getColor());
    assertEquals(LineStyle.solid, windows[1].getLineStyle());
    assertEquals(2, windows[2].getWidth());
    assertEquals(2, windows[2].getHeight());
    assertEquals(Color.BLUE, windows[2].getColor());
    assertEquals(LineStyle.solid, windows[2].getLineStyle());

    //todo: test an element wrapper around elementRefs
  }

  /**
   * tests house.  This one has things like nillable and required properties.
   */
  public void testHouse() throws Exception {
    House house = new House();
    Rectangle base = new Rectangle();
    base.setColor(Color.BLUE);
    base.setHeight(80);
    base.setWidth(80);
    base.setLineStyle(LineStyle.solid);
    base.setId("baseid");
    house.setBase(base);
    Rectangle door = new Rectangle();
    door.setColor(Color.YELLOW);
    door.setHeight(35);
    door.setWidth(20);
    door.setLineStyle(LineStyle.solid);
    door.setId("doorId");
    house.setDoor(door);
    Circle knob = new Circle();
    knob.setColor(Color.RED);
    knob.setId("knobId");
    knob.setLineStyle(LineStyle.dashed);
    knob.setRadius(2);
    house.setDoorKnob(knob);
    Label label1 = new Label();
    label1.setValue("bachelor-pad");
    Label label2 = new Label();
    label2.setValue("single-family-dwelling");
    house.setLabels(Arrays.asList(label1, label2));
    Triangle roof = new Triangle();
    roof.setBase(84);
    roof.setHeight(20);
    roof.setColor(Color.YELLOW);
    roof.setLineStyle(LineStyle.solid);
    house.setRoof(roof);
    Rectangle window = new Rectangle();
    window.setColor(Color.YELLOW);
    window.setHeight(10);
    window.setWidth(10);
    window.setLineStyle(LineStyle.solid);
    house.setWindows(Arrays.asList(window));
    house.setConstructedDate(new DateTime(3L));

    JacksonJaxbJsonProvider provider = new JacksonJaxbJsonProvider();
    ObjectMapper houseMapper = provider.locateMapper(House.class, MediaType.APPLICATION_JSON_TYPE);
    ObjectMapper clientMapper = new ObjectMapper();
    ByteArrayOutputStream out = new ByteArrayOutputStream();
    houseMapper.writeValue(out, house);
    ByteArrayInputStream in = new ByteArrayInputStream(out.toByteArray());
    shapes.json.structures.House clientHouse = clientMapper.readValue(in, shapes.json.structures.House.class);

    shapes.json.Rectangle clientBase = clientHouse.getBase();
    assertSame(shapes.json.Color.BLUE, clientBase.getColor());
    assertSame(shapes.json.LineStyle.solid, clientBase.getLineStyle());
    assertEquals(80, clientBase.getHeight());
    assertEquals(80, clientBase.getWidth());
    assertEquals("baseid", clientBase.getId());
    shapes.json.Rectangle clientDoor = clientHouse.getDoor();
    assertSame(shapes.json.Color.YELLOW, clientDoor.getColor());
    assertSame(shapes.json.LineStyle.solid, clientDoor.getLineStyle());
    assertEquals(35, clientDoor.getHeight());
    assertEquals(20, clientDoor.getWidth());
    assertEquals("doorId", clientDoor.getId());
    shapes.json.Circle clientKnob = clientHouse.getDoorKnob();
    assertSame(shapes.json.Color.RED, clientKnob.getColor());
    assertSame(shapes.json.LineStyle.dashed, clientKnob.getLineStyle());
    assertEquals(2, clientKnob.getRadius());
    assertEquals("knobId", clientKnob.getId());
    List<String> labels = Arrays.asList("bachelor-pad", "single-family-dwelling");
    clientHouse.getLabels().size();
    for (Object l : clientHouse.getLabels()) {
      shapes.json.Label label = (shapes.json.Label) l;
      assertTrue(labels.contains(label.getValue()));
    }
    shapes.json.Triangle clientRoof = clientHouse.getRoof();
    assertSame(shapes.json.Color.YELLOW, clientRoof.getColor());
    assertSame(shapes.json.LineStyle.solid, clientRoof.getLineStyle());
    assertNull(clientRoof.getId());
    assertEquals(84, clientRoof.getBase());
    assertEquals(20, clientRoof.getHeight());
    assertEquals(1, clientHouse.getWindows().size());
    shapes.json.Rectangle clientWindow = (shapes.json.Rectangle) clientHouse.getWindows().get(0);
    assertSame(shapes.json.Color.YELLOW, clientWindow.getColor());
    assertSame(shapes.json.LineStyle.solid, clientWindow.getLineStyle());
    assertEquals(10, clientWindow.getHeight());
    assertEquals(10, clientWindow.getWidth());
    assertNull(clientWindow.getId());
    assertEquals(new Date(3L), clientHouse.getConstructedDate());

    out = new ByteArrayOutputStream();
    clientMapper.writeValue(out, clientHouse);
    house = houseMapper.readValue(new ByteArrayInputStream(out.toByteArray()), House.class);
    base = house.getBase();
    assertSame(Color.BLUE, base.getColor());
    assertSame(LineStyle.solid, base.getLineStyle());
    assertEquals(80, base.getHeight());
    assertEquals(80, base.getWidth());
    assertEquals("baseid", base.getId());
    door = house.getDoor();
    assertSame(Color.YELLOW, door.getColor());
    assertSame(LineStyle.solid, door.getLineStyle());
    assertEquals(35, door.getHeight());
    assertEquals(20, door.getWidth());
    assertEquals("doorId", door.getId());
    knob = house.getDoorKnob();
    assertSame(Color.RED, knob.getColor());
    assertSame(LineStyle.dashed, knob.getLineStyle());
    assertEquals(2, knob.getRadius());
    assertEquals("knobId", knob.getId());
    labels = Arrays.asList("bachelor-pad", "single-family-dwelling");
    house.getLabels().size();
    for (Object l : house.getLabels()) {
      Label label = (Label) l;
      assertTrue(labels.contains(label.getValue()));
    }
    roof = house.getRoof();
    assertSame(Color.YELLOW, roof.getColor());
    assertSame(LineStyle.solid, roof.getLineStyle());
    assertNull(roof.getId());
    assertEquals(84, roof.getBase());
    assertEquals(20, roof.getHeight());
    assertEquals(1, house.getWindows().size());
    window = house.getWindows().get(0);
    assertSame(Color.YELLOW, window.getColor());
    assertSame(LineStyle.solid, window.getLineStyle());
    assertEquals(10, window.getHeight());
    assertEquals(10, window.getWidth());
    assertNull(window.getId());
    assertEquals(new DateTime(3L), house.getConstructedDate());

    //now let's check the nillable and required stuff:

    //roof is required, but nillable.
    clientHouse.setRoof(null);
    out = new ByteArrayOutputStream();
    clientMapper.writeValue(out, clientHouse);
    house = houseMapper.readValue(new ByteArrayInputStream(out.toByteArray()), House.class);
    base = house.getBase();
    assertSame(Color.BLUE, base.getColor());
    assertSame(LineStyle.solid, base.getLineStyle());
    assertEquals(80, base.getHeight());
    assertEquals(80, base.getWidth());
    assertEquals("baseid", base.getId());
    door = house.getDoor();
    assertSame(Color.YELLOW, door.getColor());
    assertSame(LineStyle.solid, door.getLineStyle());
    assertEquals(35, door.getHeight());
    assertEquals(20, door.getWidth());
    assertEquals("doorId", door.getId());
    knob = house.getDoorKnob();
    assertSame(Color.RED, knob.getColor());
    assertSame(LineStyle.dashed, knob.getLineStyle());
    assertEquals(2, knob.getRadius());
    assertEquals("knobId", knob.getId());
    labels = Arrays.asList("bachelor-pad", "single-family-dwelling");
    house.getLabels().size();
    for (Object l : house.getLabels()) {
      Label label = (Label) l;
      assertTrue(labels.contains(label.getValue()));
    }
    roof = house.getRoof();
    assertNull(roof);
    assertEquals(1, house.getWindows().size());
    window = house.getWindows().get(0);
    assertSame(Color.YELLOW, window.getColor());
    assertSame(LineStyle.solid, window.getLineStyle());
    assertEquals(10, window.getHeight());
    assertEquals(10, window.getWidth());
    assertNull(window.getId());

    //windows are nillable...
    clientHouse.setWindows(null);
    out = new ByteArrayOutputStream();
    clientMapper.writeValue(out, clientHouse);
    house = houseMapper.readValue(new ByteArrayInputStream(out.toByteArray()), House.class);
    base = house.getBase();
    assertSame(Color.BLUE, base.getColor());
    assertSame(LineStyle.solid, base.getLineStyle());
    assertEquals(80, base.getHeight());
    assertEquals(80, base.getWidth());
    assertEquals("baseid", base.getId());
    door = house.getDoor();
    assertSame(Color.YELLOW, door.getColor());
    assertSame(LineStyle.solid, door.getLineStyle());
    assertEquals(35, door.getHeight());
    assertEquals(20, door.getWidth());
    assertEquals("doorId", door.getId());
    knob = house.getDoorKnob();
    assertSame(Color.RED, knob.getColor());
    assertSame(LineStyle.dashed, knob.getLineStyle());
    assertEquals(2, knob.getRadius());
    assertEquals("knobId", knob.getId());
    labels = Arrays.asList("bachelor-pad", "single-family-dwelling");
    house.getLabels().size();
    for (Object l : house.getLabels()) {
      Label label = (Label) l;
      assertTrue(labels.contains(label.getValue()));
    }
    roof = house.getRoof();
    assertNull(roof);
    assertNull(house.getWindows());
  }

  /**
   * tests cat.  This one has IDREFs.
   */
  public void testCat() throws Exception {
    Cat cat = new Cat();
    Circle face = new Circle();
    face.setRadius(20);
    cat.setFace(face);
    Triangle ear = new Triangle();
    ear.setBase(5);
    ear.setHeight(10);
    ear.setId("earId");
    cat.setEars(Arrays.asList(ear, ear));

    // The eyes are the same as the ears, but so it needs to be for this test.
    cat.setEyes(new Triangle[] {ear, ear});

    Line noseLine = new Line();
    noseLine.setId("noseId");
    Line mouthLine = new Line();
    mouthLine.setId("mouthLine");

    cat.setNose(noseLine);
    cat.setMouth(mouthLine);
    cat.setWhiskers(Arrays.asList(noseLine, mouthLine));

    JacksonJaxbJsonProvider provider = new JacksonJaxbJsonProvider();
    ObjectMapper catMapper = provider.locateMapper(Cat.class, MediaType.APPLICATION_JSON_TYPE);
    ObjectMapper clientMapper = new ObjectMapper();
    ByteArrayOutputStream out = new ByteArrayOutputStream();
    catMapper.writeValue(out, cat);
    shapes.json.animals.Cat clientCat = clientMapper.readValue(new ByteArrayInputStream(out.toByteArray()), shapes.json.animals.Cat.class);

    shapes.json.Circle clientFace = clientCat.getFace();
    assertEquals(20, clientFace.getRadius());
    assertEquals(2, clientCat.getEars().size());
    shapes.json.Triangle[] clientEars = (shapes.json.Triangle[]) clientCat.getEars().toArray(new shapes.json.Triangle[2]);
    assertNotSame("referential integrity should NOT have been preserved since Jackson doesn't support it yet", clientEars[0], clientEars[1]);
    assertEquals(5, clientEars[0].getBase());
    assertEquals(10, clientEars[0].getHeight());
    assertEquals("earId", clientEars[0].getId());
    assertEquals(5, clientEars[1].getBase());
    assertEquals(10, clientEars[1].getHeight());
    assertEquals("earId", clientEars[1].getId());

    shapes.json.Triangle[] clientEyes = clientCat.getEyes();
    assertEquals(2, clientEyes.length);
    assertNotSame(clientEyes[0], clientEyes[1]);
    assertEquals(5, clientEyes[0].getBase());
    assertEquals(10, clientEyes[0].getHeight());
    assertEquals("earId", clientEyes[0].getId());
    assertEquals(5, clientEyes[1].getBase());
    assertEquals(10, clientEyes[1].getHeight());
    assertEquals("earId", clientEyes[1].getId());
    assertFalse("The ears should NOT be the same object as one of the eyes since Jackson doesn't support referential integrity.", clientEars[0] == clientEyes[0] || clientEars[0] == clientEyes[1]);

    shapes.json.Line clientNose = clientCat.getNose();
    assertEquals("noseId", clientNose.getId());
    shapes.json.Line clientMouth = clientCat.getMouth();
    assertEquals("mouthLine", clientMouth.getId());
    assertFalse("The nose line should NOT also be one of the whiskers since Jackson doesn't support referential integrity.", clientCat.getWhiskers().contains(clientNose));
    assertFalse("The mouth line should NOT also be one of the whiskers since Jackson doesn't support referential integrity.", clientCat.getWhiskers().contains(clientMouth));

    out = new ByteArrayOutputStream();
    clientMapper.writeValue(out, clientCat);
    cat = catMapper.readValue(new ByteArrayInputStream(out.toByteArray()), Cat.class);

    face = cat.getFace();
    assertEquals(20, face.getRadius());
    assertEquals(2, cat.getEars().size());
    Triangle[] ears = cat.getEars().toArray(new Triangle[2]);
    assertNotSame("referential integrity should NOT have been preserved since Jackson doesn't support referential integrity.", ears[0], ears[1]);
    assertEquals(5, ears[0].getBase());
    assertEquals(10, ears[0].getHeight());
    assertEquals("earId", ears[0].getId());

    Triangle[] eyes = cat.getEyes();
    assertEquals(2, eyes.length);
    assertNotSame(eyes[0], eyes[1]);
    assertEquals(5, eyes[0].getBase());
    assertEquals(10, eyes[0].getHeight());
    assertEquals("earId", eyes[0].getId());
    assertEquals(5, eyes[1].getBase());
    assertEquals(10, eyes[1].getHeight());
    assertEquals("earId", eyes[1].getId());
    assertFalse("The ears should NOT be the same object as one of the eyes since Jackson doesn't support referential integrity.", ears[0] == eyes[0] || ears[0] == eyes[1]);

    Line nose = cat.getNose();
    assertEquals("noseId", nose.getId());
    Line mouth = cat.getMouth();
    assertEquals("mouthLine", mouth.getId());
    assertFalse("The nose line should also be one of the whiskers since Jackson doesn't support referential integrity.", cat.getWhiskers().contains(nose));
    assertFalse("The mouth line should also be one of the whiskers since Jackson doesn't support referential integrity.", cat.getWhiskers().contains(mouth));

  }

  /**
   * tests the canvas.  This one as XmlElementRefs, XmlElements, and an attachment...
   */
  public void testCanvas() throws Exception {
    Canvas canvas = new Canvas();
    Bus bus = new Bus();
    bus.setId("busId");
    Rectangle busFrame = new Rectangle();
    busFrame.setWidth(100);
    bus.setFrame(busFrame);
    Cat cat = new Cat();
    cat.setId("catId");
    Circle catFace = new Circle();
    catFace.setRadius(30);
    cat.setFace(catFace);
    House house = new House();
    house.setId("houseId");
    Rectangle houseBase = new Rectangle();
    houseBase.setWidth(76);
    house.setBase(houseBase);
    canvas.setFigures(Arrays.asList(bus, cat, house));
    Rectangle rectangle = new Rectangle();
    rectangle.setHeight(50);
    rectangle.setId("rectId");
    Circle circle = new Circle();
    circle.setRadius(10);
    circle.setId("circleId");
    Triangle triangle = new Triangle();
    triangle.setBase(80);
    triangle.setId("triId");
    canvas.setShapes(Arrays.asList(rectangle, circle, triangle));
    byte[] swaRefBytes = "This is a bunch of random bytes that are to be used as an SWA ref attachment.".getBytes();
    byte[] explicitBase64Bytes = "This is some more random bytes that are to be used as a base 64 encoded attachment.".getBytes();
    byte[] attachment1Bytes = "This is some more random bytes that are to be used as the first MTOM attachment.".getBytes();
    byte[] attachment2Bytes = "This is some more random bytes that are to be used as the second MTOM attachment.".getBytes();
    byte[] attachment3Bytes = "This is some more random bytes that are to be used as the third MTOM attachment.".getBytes();
    CanvasAttachment attachment1 = new CanvasAttachment();
    attachment1.setValue(attachment1Bytes);
    CanvasAttachment attachment2 = new CanvasAttachment();
    attachment2.setValue(attachment2Bytes);
    CanvasAttachment attachment3 = new CanvasAttachment();
    attachment3.setValue(attachment3Bytes);
    ByteArrayDataSource dataSource = new ByteArrayDataSource(swaRefBytes, "application/octet-stream");
    dataSource.setName("somename");
    //todo: uncomment when JAXB bug is fixed
//    canvas.setBackgroundImage(new DataHandler(dataSource));
    canvas.setExplicitBase64Attachment(explicitBase64Bytes);
    canvas.setOtherAttachments(Arrays.asList(attachment1, attachment2, attachment3));

    JacksonJaxbJsonProvider provider = new JacksonJaxbJsonProvider();
    ObjectMapper canvasMapper = provider.locateMapper(Canvas.class, MediaType.APPLICATION_JSON_TYPE);
    ObjectMapper clientMapper = new ObjectMapper();
    ByteArrayOutputStream out = new ByteArrayOutputStream();
    canvasMapper.writeValue(out, canvas);
    //set up the attachments that were written

    shapes.json.draw.Canvas clientCanvas = clientMapper.readValue(new ByteArrayInputStream(out.toByteArray()), shapes.json.draw.Canvas.class);
    Collection<ObjectNode> clientShapes = clientCanvas.getShapes();
    assertEquals(3, clientShapes.size());
    for (ObjectNode shape : clientShapes) {
      if (shape.get("radius") != null) {
        assertEquals("circleId", shape.get("id").getTextValue());
        assertEquals(10, shape.get("radius").getIntValue());
      }
      else if (shape.get("base") != null) {
        assertEquals("triId", shape.get("id").getTextValue());
        assertEquals(80, shape.get("base").getIntValue());
      }
      else if (shape.get("height") != null) {
        assertEquals("rectId", shape.get("id").getTextValue());
        assertEquals(50, shape.get("height").getIntValue());
      }
      else {
        fail("Unknown shape: " + shape);
      }
    }

    Collection<ObjectNode> clientFigures = clientCanvas.getFigures();
    assertEquals(3, clientFigures.size());
    for (ObjectNode figure : clientFigures) {
      assertEquals(1, figure.size());
      String figureKey = figure.getFields().next().getKey();
      figure = (ObjectNode) figure.get(figureKey);
      if ("bus".equals(figureKey)) {
        assertEquals("busId", figure.get("id").getTextValue());
        assertTrue(figure.get("frame") != null);
        assertEquals(100, figure.get("frame").get("width").getIntValue());
      }
      else if ("cat".equals(figureKey)) {
        assertEquals("catId", figure.get("id").getTextValue());
        assertTrue(figure.get("circle") != null);
        assertEquals(30, figure.get("circle").get("radius").getIntValue());
      }
      else if ("house".equals(figureKey)) {
        assertEquals("houseId", figure.get("id").getTextValue());
        assertTrue(figure.get("base") != null);
        assertEquals(76, figure.get("base").get("width").getIntValue());
      }
      else {
        fail("Unknown figure: " + figure);
      }
    }

//    DataHandler backgroundImage = clientCanvas.getBackgroundImage();
//    InputStream attachmentStream = backgroundImage.getInputStream();
//    ByteArrayOutputStream bgImageIn = new ByteArrayOutputStream();
//    int byteIn = attachmentStream.read();
//    while (byteIn > 0) {
//      bgImageIn.write(byteIn);
//      byteIn = attachmentStream.read();
//    }
//    assertTrue(Arrays.equals(swaRefBytes, bgImageIn.toByteArray()));

    byte[] base64Attachment = clientCanvas.getExplicitBase64Attachment();
    assertNotNull(base64Attachment);
    assertTrue(Arrays.equals(explicitBase64Bytes, base64Attachment));

    Collection otherAttachments = clientCanvas.getOtherAttachments();
    assertEquals(3, otherAttachments.size());
    Iterator attachmentsIt = otherAttachments.iterator();
    int attachmentCount = 0;
    while (attachmentsIt.hasNext()) {
      shapes.json.draw.CanvasAttachment otherAttachment = (shapes.json.draw.CanvasAttachment) attachmentsIt.next();
      byte[] otherAttachmentBytes = otherAttachment.getValue();
      if (Arrays.equals(attachment1Bytes, otherAttachmentBytes)) {
        attachmentCount++;
      }
      else if (Arrays.equals(attachment2Bytes, otherAttachmentBytes)) {
        attachmentCount++;
      }
      else if (Arrays.equals(attachment3Bytes, otherAttachmentBytes)) {
        attachmentCount++;
      }
      else {
        fail("Unknown attachment.");
      }
    }
    assertEquals(3, attachmentCount);

    clientCanvas.setShapes(null); //@XmlElementRefs can't be (de)serialized.
    out = new ByteArrayOutputStream();
    clientMapper.writeValue(out, clientCanvas);
    canvas = canvasMapper.readValue(new ByteArrayInputStream(out.toByteArray()), Canvas.class);

    Collection shapes = canvas.getShapes();
    assertNull(shapes);
//    assertEquals(3, shapes.size());
//    for (Object Shape : shapes) {
//      if (Shape instanceof Circle) {
//        assertEquals("circleId", ((Circle) Shape).getId());
//        assertEquals(10, ((Circle) Shape).getRadius());
//      }
//      else if (Shape instanceof Rectangle) {
//        assertEquals("rectId", ((Rectangle) Shape).getId());
//        assertEquals(50, ((Rectangle) Shape).getHeight());
//      }
//      else if (Shape instanceof Triangle) {
//        assertEquals("triId", ((Triangle) Shape).getId());
//        assertEquals(80, ((Triangle) Shape).getBase());
//      }
//      else {
//        fail("Unknown shape: " + Shape);
//      }
//    }

    Collection figures = canvas.getFigures();
    assertEquals(3, figures.size());
    for (Object Figure : figures) {
      if (Figure instanceof Bus) {
        bus = (Bus) Figure;
        assertEquals("busId", bus.getId());
        Rectangle BusFrame = bus.getFrame();
        assertNotNull(BusFrame);
        assertEquals(100, busFrame.getWidth());
      }
      else if (Figure instanceof Cat) {
        cat = (Cat) Figure;
        assertEquals("catId", cat.getId());
        Circle CatFace = cat.getFace();
        assertNotNull(CatFace);
        assertEquals(30, CatFace.getRadius());
      }
      else if (Figure instanceof House) {
        house = (House) Figure;
        assertEquals("houseId", house.getId());
        Rectangle HouseBase = house.getBase();
        assertNotNull(HouseBase);
        assertEquals(76, HouseBase.getWidth());
      }
      else {
        fail("Unknown figure: " + Figure);
      }
    }

//    backgroundImage = canvas.getBackgroundImage();
//    attachmentStream = backgroundImage.getInputStream();
//    bgImageIn = new ByteArrayOutputStream();
//    byteIn = attachmentStream.read();
//    while (byteIn > 0) {
//      bgImageIn.write(byteIn);
//      byteIn = attachmentStream.read();
//    }
//
//    assertTrue(Arrays.equals(swaRefBytes, bgImageIn.toByteArray()));

    base64Attachment = canvas.getExplicitBase64Attachment();
    assertNotNull(base64Attachment);
    assertTrue(Arrays.equals(explicitBase64Bytes, base64Attachment));

    otherAttachments = canvas.getOtherAttachments();
    assertEquals(3, otherAttachments.size());
    attachmentsIt = otherAttachments.iterator();
    attachmentCount = 0;
    while (attachmentsIt.hasNext()) {
      CanvasAttachment otherAttachment = (CanvasAttachment) attachmentsIt.next();
      byte[] otherAttachmentBytes = otherAttachment.getValue();
      if (Arrays.equals(attachment1Bytes, otherAttachmentBytes)) {
        attachmentCount++;
      }
      else if (Arrays.equals(attachment2Bytes, otherAttachmentBytes)) {
        attachmentCount++;
      }
      else if (Arrays.equals(attachment3Bytes, otherAttachmentBytes)) {
        attachmentCount++;
      }
      else {
        fail("Unknown attachment.");
      }
    }
    assertEquals(3, attachmentCount);

    //todo: test element ref to an attachment element
    //todo: test element refs of attachment elements.
  }
}
