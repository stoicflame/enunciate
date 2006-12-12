package net.sf.enunciate.modules.xfire_client.xfire_types;

import junit.framework.TestCase;
import net.sf.enunciate.examples.xfire_client.schema.Rectangle;
import net.sf.enunciate.examples.xfire_client.schema.Color;
import net.sf.enunciate.examples.xfire_client.schema.Circle;
import net.sf.enunciate.examples.xfire_client.schema.LineStyle;
import net.sf.enunciate.examples.xfire_client.schema.Triangle;
import net.sf.enunciate.examples.xfire_client.schema.Label;
import net.sf.enunciate.examples.xfire_client.schema.Line;
import net.sf.enunciate.examples.xfire_client.schema.animals.Cat;
import net.sf.enunciate.examples.xfire_client.schema.structures.House;
import net.sf.enunciate.examples.xfire_client.schema.vehicles.Bus;
import net.sf.enunciate.modules.xfire_client.IntrospectingTypeRegistry;
import org.codehaus.xfire.MessageContext;
import org.codehaus.xfire.aegis.stax.ElementReader;
import org.codehaus.xfire.aegis.stax.ElementWriter;
import shapes.*;
import shapes.animals.CatXFireType;
import shapes.structures.HouseXFireType;
import shapes.vehicles.BusXFireType;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;
import javax.xml.namespace.QName;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * This is a special test case that depends on the sample schema included in this module.
 *
 * @author Ryan Heaton
 */
public class TestXFireTypes extends TestCase {

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

    JAXBContext context = JAXBContext.newInstance(Circle.class, Rectangle.class, Triangle.class);
    Marshaller marshaller = context.createMarshaller();
    Unmarshaller unmarshaller = context.createUnmarshaller();
    IntrospectingTypeRegistry typeRegistry = new IntrospectingTypeRegistry("shapes");

    ByteArrayOutputStream out = new ByteArrayOutputStream();
    marshaller.marshal(circle, out);
    ByteArrayInputStream in = new ByteArrayInputStream(out.toByteArray());

    CircleXFireType circleType = (CircleXFireType) typeRegistry.getDefaultTypeMapping().getType(shapes.Circle.class);
    shapes.Circle clientCircle = (shapes.Circle) circleType.readObject(new ElementReader(in), new MessageContext());
    assertSame(shapes.Color.BLUE, clientCircle.getColor());
    assertEquals("someid", clientCircle.getId());
    assertEquals(shapes.LineStyle.solid, clientCircle.getLineStyle());
    assertEquals(8, clientCircle.getPositionX());
    assertEquals(9, clientCircle.getPositionY());
    assertEquals(10, clientCircle.getRadius());

    out = new ByteArrayOutputStream();
    QName rootElementName = circleType.getRootElementName();
    ElementWriter writer = new ElementWriter(out, rootElementName.getLocalPart(), rootElementName.getNamespaceURI());
    circleType.writeObject(clientCircle, writer, new MessageContext());
    writer.close();
    writer.getXMLStreamWriter().close();
    in = new ByteArrayInputStream(out.toByteArray());

    circle = (Circle) unmarshaller.unmarshal(in);
    assertSame(Color.BLUE, circle.getColor());
    assertEquals("someid", circle.getId());
    assertEquals(LineStyle.solid, circle.getLineStyle());
    assertEquals(8, circle.getPositionX());
    assertEquals(9, circle.getPositionY());
    assertEquals(10, circle.getRadius());

    Rectangle rectangle = new Rectangle();
    rectangle.setColor(Color.GREEN);
    rectangle.setId("rectid");
    rectangle.setHeight(500);
    rectangle.setWidth(1000);
    rectangle.setLineStyle(LineStyle.dotted);
    rectangle.setPositionX(-100);
    rectangle.setPositionY(-300);

    out = new ByteArrayOutputStream();
    marshaller.marshal(rectangle, out);
    in = new ByteArrayInputStream(out.toByteArray());

    RectangleXFireType rectType = (RectangleXFireType) typeRegistry.getDefaultTypeMapping().getType(shapes.Rectangle.class);
    shapes.Rectangle clientRect = (shapes.Rectangle) rectType.readObject(new ElementReader(in), new MessageContext());
    assertSame(shapes.Color.GREEN, clientRect.getColor());
    assertEquals("rectid", clientRect.getId());
    assertEquals(shapes.LineStyle.dotted, clientRect.getLineStyle());
    assertEquals(500, clientRect.getHeight());
    assertEquals(1000, clientRect.getWidth());
    assertEquals(-100, clientRect.getPositionX());
    assertEquals(-300, clientRect.getPositionY());

    out = new ByteArrayOutputStream();
    rootElementName = rectType.getRootElementName();
    writer = new ElementWriter(out, rootElementName.getLocalPart(), rootElementName.getNamespaceURI());
    rectType.writeObject(clientRect, writer, new MessageContext());
    writer.close();
    writer.getXMLStreamWriter().close();
    in = new ByteArrayInputStream(out.toByteArray());

    rectangle = (Rectangle) unmarshaller.unmarshal(in);
    assertSame(Color.GREEN, rectangle.getColor());
    assertEquals("rectid", rectangle.getId());
    assertEquals(LineStyle.dotted, rectangle.getLineStyle());
    assertEquals(500, rectangle.getHeight());
    assertEquals(1000, rectangle.getWidth());
    assertEquals(-100, rectangle.getPositionX());
    assertEquals(-300, rectangle.getPositionY());

    Triangle triangle = new Triangle();
    triangle.setBase(90);
    triangle.setColor(Color.RED);
    triangle.setHeight(100);
    triangle.setId("triangleId");
    triangle.setLineStyle(LineStyle.dashed);
    triangle.setPositionX(0);
    triangle.setPositionY(-10);

    out = new ByteArrayOutputStream();
    marshaller.marshal(triangle, out);
    in = new ByteArrayInputStream(out.toByteArray());

    TriangleXFireType triType = (TriangleXFireType) typeRegistry.getDefaultTypeMapping().getType(shapes.Triangle.class);
    shapes.Triangle clientTri = (shapes.Triangle) triType.readObject(new ElementReader(in), new MessageContext());
    assertSame(shapes.Color.RED, clientTri.getColor());
    assertEquals("triangleId", clientTri.getId());
    assertEquals(shapes.LineStyle.dashed, clientTri.getLineStyle());
    assertEquals(90, clientTri.getBase());
    assertEquals(100, clientTri.getHeight());
    assertEquals(0, clientTri.getPositionX());
    assertEquals(-10, clientTri.getPositionY());

    out = new ByteArrayOutputStream();
    rootElementName = triType.getRootElementName();
    writer = new ElementWriter(out, rootElementName.getLocalPart(), rootElementName.getNamespaceURI());
    triType.writeObject(clientTri, writer, new MessageContext());
    writer.close();
    writer.getXMLStreamWriter().close();
    in = new ByteArrayInputStream(out.toByteArray());

    triangle = (Triangle) unmarshaller.unmarshal(in);
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

    JAXBContext context = JAXBContext.newInstance(Bus.class);
    Marshaller marshaller = context.createMarshaller();
    Unmarshaller unmarshaller = context.createUnmarshaller();
    IntrospectingTypeRegistry typeRegistry = new IntrospectingTypeRegistry("shapes");

    ByteArrayOutputStream out = new ByteArrayOutputStream();
    marshaller.marshal(bus, out);
    ByteArrayInputStream in = new ByteArrayInputStream(out.toByteArray());
    BusXFireType busType = (BusXFireType) typeRegistry.getDefaultTypeMapping().getType(shapes.vehicles.Bus.class);
    shapes.vehicles.Bus clientBus = (shapes.vehicles.Bus) busType.readObject(new ElementReader(in), new MessageContext());
    assertEquals("bus id", clientBus.getId());
    ArrayList<String> labels = new ArrayList<String>(Arrays.asList("city", "country", "long-distance"));
    for (Object l : clientBus.getLabels()) {
      shapes.Label label = (shapes.Label) l;
      assertTrue(labels.remove(label.getValue()));
    }
    shapes.Rectangle clientDoor = clientBus.getDoor();
    assertSame(shapes.Color.BLUE, clientDoor.getColor());
    assertEquals(2, clientDoor.getWidth());
    assertEquals(4, clientDoor.getHeight());
    assertSame(shapes.LineStyle.solid, clientDoor.getLineStyle());
    shapes.Rectangle clientFrame = clientBus.getFrame();
    assertEquals(10, clientFrame.getHeight());
    assertEquals(50, clientFrame.getWidth());
    assertSame(shapes.Color.YELLOW, clientFrame.getColor());
    assertSame(shapes.LineStyle.solid, clientFrame.getLineStyle());
    shapes.Circle[] clientWheels = clientBus.getWheels();
    assertEquals(2, clientWheels.length);
    assertEquals(6, clientWheels[0].getRadius());
    assertSame(shapes.Color.BLUE, clientWheels[0].getColor());
    assertSame(shapes.LineStyle.dotted, clientWheels[0].getLineStyle());
    assertEquals(7, clientWheels[1].getRadius());
    assertSame(shapes.Color.BLUE, clientWheels[1].getColor());
    assertSame(shapes.LineStyle.dotted, clientWheels[1].getLineStyle());
    shapes.Rectangle[] clientWindows = (shapes.Rectangle[]) clientBus.getWindows().toArray(new shapes.Rectangle[3]);
    assertEquals(2, clientWindows[0].getWidth());
    assertEquals(2, clientWindows[0].getHeight());
    assertEquals(shapes.Color.BLUE, clientWindows[0].getColor());
    assertEquals(shapes.LineStyle.solid, clientWindows[0].getLineStyle());
    assertEquals(2, clientWindows[1].getWidth());
    assertEquals(2, clientWindows[1].getHeight());
    assertEquals(shapes.Color.BLUE, clientWindows[1].getColor());
    assertEquals(shapes.LineStyle.solid, clientWindows[1].getLineStyle());
    assertEquals(2, clientWindows[2].getWidth());
    assertEquals(2, clientWindows[2].getHeight());
    assertEquals(shapes.Color.BLUE, clientWindows[2].getColor());
    assertEquals(shapes.LineStyle.solid, clientWindows[2].getLineStyle());

    out = new ByteArrayOutputStream();
    QName rootElementName = busType.getRootElementName();
    ElementWriter writer = new ElementWriter(out, rootElementName.getLocalPart(), rootElementName.getNamespaceURI());
    busType.writeObject(clientBus, writer, new MessageContext());
    writer.close();
    writer.getXMLStreamWriter().close();

    bus = (Bus) unmarshaller.unmarshal(new ByteArrayInputStream(out.toByteArray()));
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

    JAXBContext context = JAXBContext.newInstance(House.class);
    Marshaller marshaller = context.createMarshaller();
    ByteArrayOutputStream out = new ByteArrayOutputStream();
    marshaller.marshal(house, out);
    ByteArrayInputStream in = new ByteArrayInputStream(out.toByteArray());
    IntrospectingTypeRegistry typeRegistry = new IntrospectingTypeRegistry("shapes");
    HouseXFireType houseType = (HouseXFireType) typeRegistry.getDefaultTypeMapping().getType(shapes.structures.House.class);
    shapes.structures.House clientHouse = (shapes.structures.House) houseType.readObject(new ElementReader(in), new MessageContext());

    shapes.Rectangle clientBase = clientHouse.getBase();
    assertSame(shapes.Color.BLUE, clientBase.getColor());
    assertSame(shapes.LineStyle.solid, clientBase.getLineStyle());
    assertEquals(80, clientBase.getHeight());
    assertEquals(80, clientBase.getWidth());
    assertEquals("baseid", clientBase.getId());
    shapes.Rectangle clientDoor = clientHouse.getDoor();
    assertSame(shapes.Color.YELLOW, clientDoor.getColor());
    assertSame(shapes.LineStyle.solid, clientDoor.getLineStyle());
    assertEquals(35, clientDoor.getHeight());
    assertEquals(20, clientDoor.getWidth());
    assertEquals("doorId", clientDoor.getId());
    shapes.Circle clientKnob = clientHouse.getDoorKnob();
    assertSame(shapes.Color.RED, clientKnob.getColor());
    assertSame(shapes.LineStyle.dashed, clientKnob.getLineStyle());
    assertEquals(2, clientKnob.getRadius());
    assertEquals("knobId", clientKnob.getId());
    List<String> labels = Arrays.asList("bachelor-pad", "single-family-dwelling");
    clientHouse.getLabels().size();
    for (Object l : clientHouse.getLabels()) {
      shapes.Label label = (shapes.Label) l;
      assertTrue(labels.contains(label.getValue()));
    }
    shapes.Triangle clientRoof = clientHouse.getRoof();
    assertSame(shapes.Color.YELLOW, clientRoof.getColor());
    assertSame(shapes.LineStyle.solid, clientRoof.getLineStyle());
    assertNull(clientRoof.getId());
    assertEquals(84, clientRoof.getBase());
    assertEquals(20, clientRoof.getHeight());
    assertEquals(1, clientHouse.getWindows().size());
    shapes.Rectangle clientWindow = (shapes.Rectangle) clientHouse.getWindows().get(0);
    assertSame(shapes.Color.YELLOW, clientWindow.getColor());
    assertSame(shapes.LineStyle.solid, clientWindow.getLineStyle());
    assertEquals(10, clientWindow.getHeight());
    assertEquals(10, clientWindow.getWidth());
    assertNull(clientWindow.getId());

    out = new ByteArrayOutputStream();
    QName rootElementName = houseType.getRootElementName();
    ElementWriter writer = new ElementWriter(out, rootElementName.getLocalPart(), rootElementName.getNamespaceURI());
    houseType.writeObject(clientHouse, writer, new MessageContext());
    writer.close();
    writer.getXMLStreamWriter().close();
    Unmarshaller unmarshaller = context.createUnmarshaller();
    house = (House) unmarshaller.unmarshal(new ByteArrayInputStream(out.toByteArray()));
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

    //now let's check the nillable and required stuff:

    //roof is required, but nillable.
    clientHouse.setRoof(null);
    out = new ByteArrayOutputStream();
    writer = new ElementWriter(out, rootElementName.getLocalPart(), rootElementName.getNamespaceURI());
    houseType.writeObject(clientHouse, writer, new MessageContext());
    writer.close();
    writer.getXMLStreamWriter().close();
    house = (House) unmarshaller.unmarshal(new ByteArrayInputStream(out.toByteArray()));
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
    writer = new ElementWriter(out, rootElementName.getLocalPart(), rootElementName.getNamespaceURI());
    houseType.writeObject(clientHouse, writer, new MessageContext());
    writer.close();
    writer.getXMLStreamWriter().close();
    house = (House) unmarshaller.unmarshal(new ByteArrayInputStream(out.toByteArray()));
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

    //base is required and NOT nillable.
    clientHouse.setBase(null);
    out = new ByteArrayOutputStream();
    writer = new ElementWriter(out, rootElementName.getLocalPart(), rootElementName.getNamespaceURI());
    try {
      houseType.writeObject(clientHouse, writer, new MessageContext());
      fail("should have required a base.");
    }
    catch (NullPointerException npe) {
      //fall through.
    }
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

    JAXBContext context = JAXBContext.newInstance(Cat.class);
    Marshaller marshaller = context.createMarshaller();
    CatXFireType catType = (CatXFireType) new IntrospectingTypeRegistry("shapes").getDefaultTypeMapping().getType(shapes.animals.Cat.class);
    ByteArrayOutputStream out = new ByteArrayOutputStream();
    marshaller.marshal(cat, out);
    shapes.animals.Cat clientCat = (shapes.animals.Cat) catType.readObject(new ElementReader(new ByteArrayInputStream(out.toByteArray())), new MessageContext());

    shapes.Circle clientFace = clientCat.getFace();
    assertEquals(20, clientFace.getRadius());
    assertEquals(2, clientCat.getEars().size());
    shapes.Triangle[] clientEars = (shapes.Triangle[]) clientCat.getEars().toArray(new shapes.Triangle[2]);
    assertSame("referential integrity should have been preserved (same object for ears)", clientEars[0], clientEars[1]);
    assertEquals(5, clientEars[0].getBase());
    assertEquals(10, clientEars[0].getHeight());
    assertEquals("earId", clientEars[0].getId());

    shapes.Triangle[] clientEyes = clientCat.getEyes();
    assertEquals(2, clientEyes.length);
    assertNotSame(clientEyes[0], clientEyes[1]);
    assertEquals(5, clientEyes[0].getBase());
    assertEquals(10, clientEyes[0].getHeight());
    assertEquals("earId", clientEyes[0].getId());
    assertEquals(5, clientEyes[1].getBase());
    assertEquals(10, clientEyes[1].getHeight());
    assertEquals("earId", clientEyes[1].getId());
    assertTrue("The ears should be the same object as one of the eyes (preserve referential integrity).", clientEars[0] == clientEyes[0] || clientEars[0] == clientEyes[1]);

    shapes.Line clientNose = clientCat.getNose();
    assertEquals("noseId", clientNose.getId());
    shapes.Line clientMouth = clientCat.getMouth();
    assertEquals("mouthLine", clientMouth.getId());
    assertTrue("The nose line should also be one of the whiskers (preserve referential integrity)", clientCat.getWhiskers().contains(clientNose));
    assertTrue("The mouth line should also be one of the whiskers (preserve referential integrity)", clientCat.getWhiskers().contains(clientMouth));

    out = new ByteArrayOutputStream();
    QName rootElementName = catType.getRootElementName();
    ElementWriter writer = new ElementWriter(out, rootElementName.getLocalPart(), rootElementName.getNamespaceURI());
    catType.writeObject(clientCat, writer, new MessageContext());
    writer.close();
    writer.getXMLStreamWriter().close();
    Unmarshaller unmarshaller = context.createUnmarshaller();
    cat = (Cat) unmarshaller.unmarshal(new ByteArrayInputStream(out.toByteArray()));

    face = cat.getFace();
    assertEquals(20, face.getRadius());
    assertEquals(2, cat.getEars().size());
    Triangle[] ears = cat.getEars().toArray(new Triangle[2]);
    assertSame("referential integrity should have been preserved (same object for ears)", ears[0], ears[1]);
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
    assertTrue("The ears should be the same object as one of the eyes (preserve referential integrity).", ears[0] == eyes[0] || ears[0] == eyes[1]);

    Line nose = cat.getNose();
    assertEquals("noseId", nose.getId());
    Line mouth = cat.getMouth();
    assertEquals("mouthLine", mouth.getId());
    assertTrue("The nose line should also be one of the whiskers (preserve referential integrity)", cat.getWhiskers().contains(nose));
    assertTrue("The mouth line should also be one of the whiskers (preserve referential integrity)", cat.getWhiskers().contains(mouth));

    cat.setWhiskers(null);
    cat.setEyes(null);

    out = new ByteArrayOutputStream();
    marshaller.marshal(cat, out);
    clientCat = (shapes.animals.Cat) catType.readObject(new ElementReader(new ByteArrayInputStream(out.toByteArray())), new MessageContext());
    assertNull("No mouth should have been added because there was no object reached that has that id.", clientCat.getMouth());
    assertNull("No nose should have been added because there was no object reached that has that id.", clientCat.getNose());
    assertNull("No ears should have been added because there was no object reached that has that id.", clientCat.getEars());
  }

  /**
   * tests the canvas.  This one as XmlElementRefs, XmlElements, and an attachment...
   */
  public void testCanvas() throws Exception {
    
  }

}
