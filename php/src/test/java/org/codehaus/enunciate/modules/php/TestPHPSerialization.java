package org.codehaus.enunciate.modules.php;

import junit.framework.TestCase;

import java.io.*;
import java.util.*;

import org.codehaus.enunciate.examples.php.schema.*;
import org.codehaus.enunciate.examples.php.schema.vehicles.*;
import org.codehaus.enunciate.examples.php.schema.structures.*;
import org.codehaus.enunciate.examples.php.schema.draw.*;
import org.codehaus.enunciate.examples.php.schema.animals.*;
import org.codehaus.jackson.jaxrs.JacksonJaxbJsonProvider;
import org.codehaus.jackson.map.*;
import org.codehaus.jackson.map.introspect.Annotated;
import org.codehaus.jackson.xc.JaxbAnnotationIntrospector;
import org.codehaus.jackson.JsonGenerator;
import org.codehaus.jackson.JsonProcessingException;
import org.codehaus.jackson.JsonParser;

import javax.activation.DataHandler;

/**
 * Makes sure PHP serialization is working correctly.
 *
 * @author Ryan Heaton
 */
public class TestPHPSerialization extends TestCase {

  private boolean skipPHPTests;
  private String phpExe;
  private File tempDir;
  private File exe;

  @Override
  protected void setUp() throws Exception {
    super.setUp();

    String skipPHPTests = System.getProperty("skip.php.tests");
    this.skipPHPTests = "true".equalsIgnoreCase(skipPHPTests);

    if (!this.skipPHPTests) {
      this.phpExe = System.getProperty("php.execuable");
      if (this.phpExe == null) {
        throw new IllegalStateException("The php executable must be supplied via property 'php.executable'.");
      }

      String exe = System.getProperty("processjson.php");
      if (exe == null) {
        throw new IllegalStateException("The path to the processjson script must be supplied via property 'processjson.php'.");
      }
      this.exe = new File(exe);

      String tempDir = System.getProperty("json.tempdir");
      if (tempDir == null) {
        throw new IllegalStateException("The temp directory to put the JSON files must be supplied via the 'json.tempdir' property.");
      }
      this.tempDir = new File(tempDir);
    }
  }

  /**
   * tests serialization.
   */
  public void testSerializeDeserialize() throws Exception {
    if (this.skipPHPTests) {
      System.out.println("PHP tests have been disabled.");
      return;
    }

    Circle circle = new Circle();
    circle.setColor(Color.BLUE);
    circle.setId("someid");
    circle.setLineStyle(LineStyle.solid);
    circle.setPositionX(8);
    circle.setPositionY(9);
    circle.setRadius(10);
    circle = processThroughJson(circle);
    assertEquals(Color.BLUE, circle.getColor());
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
    rectangle = processThroughJson(rectangle);
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
    triangle = processThroughJson(triangle);
    assertSame(Color.RED, triangle.getColor());
    assertEquals(90, triangle.getBase());
    assertEquals(100, triangle.getHeight());
    assertEquals("triangleId", triangle.getId());
    assertSame(LineStyle.dashed, triangle.getLineStyle());
    assertEquals(0, triangle.getPositionX());
    assertEquals(-10, triangle.getPositionY());
  }

  /**
   * tests a bus
   */
  public void testBus() throws Exception {
    if (this.skipPHPTests) {
      System.out.println("PHP tests have been disabled.");
      return;
    }

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
    bus.setWheels(new Circle[]{front, back});
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
    bus = processThroughJson(bus);
    assertEquals("bus id", bus.getId());
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
    if (this.skipPHPTests) {
      System.out.println("PHP tests have been disabled.");
      return;
    }

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
    Date date = new Date();
//    house.setConstructedDate(new DateTime(date, DateTimeZone.UTC));

    house = processThroughJson(house);

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
    List<String> labels = Arrays.asList("bachelor-pad", "single-family-dwelling");
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
    assertNull("constructed date should be null (php value type should not be serialized)", house.getConstructedDate());
    //todo: figure out the timezone particulars.
    //assertEquals(new DateTime(date, DateTimeZone.UTC), house.getConstructedDate());

  }

  /**
   * tests cat.  This one has IDREFs.
   * todo: worry about xmlids
   */
  public void x_testCat() throws Exception {
    if (this.skipPHPTests) {
      System.out.println("PHP tests have been disabled.");
      return;
    }

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
    cat.setEyes(new Triangle[]{ear, ear});

    Line noseLine = new Line();
    noseLine.setId("noseId");
    Line mouthLine = new Line();
    mouthLine.setId("mouthLine");

    cat.setNose(noseLine);
    cat.setMouth(mouthLine);
    cat.setWhiskers(Arrays.asList(noseLine, mouthLine));

    cat = processThroughJson(cat);

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

  }

  /**
   * tests the canvas.  This one as XmlElementRefs, XmlElements, and an attachment...
   */
  public void testCanvas() throws Exception {
    if (this.skipPHPTests) {
      System.out.println("PHP tests have been disabled.");
      return;
    }

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
    //canvas.setShapes(Arrays.asList(rectangle, circle, triangle));
//    byte[] swaRefBytes = "This is a bunch of random bytes that are to be used as an SWA ref attachment.".getBytes();
//    byte[] explicitBase64Bytes = "This is some more random bytes that are to be used as a base 64 encoded attachment.".getBytes();
//    byte[] attachment1Bytes = "This is some more random bytes that are to be used as the first MTOM attachment.".getBytes();
//    byte[] attachment2Bytes = "This is some more random bytes that are to be used as the second MTOM attachment.".getBytes();
//    byte[] attachment3Bytes = "This is some more random bytes that are to be used as the third MTOM attachment.".getBytes();
//    CanvasAttachment attachment1 = new CanvasAttachment();
//    attachment1.setValue(attachment1Bytes);
//    CanvasAttachment attachment2 = new CanvasAttachment();
//    attachment2.setValue(attachment2Bytes);
//    CanvasAttachment attachment3 = new CanvasAttachment();
//    attachment3.setValue(attachment3Bytes);
//    ByteArrayDataSource dataSource = new ByteArrayDataSource(swaRefBytes, "application/octet-stream");
//    dataSource.setName("somename");
//    canvas.setBackgroundImage(new DataHandler(dataSource));
//    canvas.setExplicitBase64Attachment(explicitBase64Bytes);
//    canvas.setOtherAttachments(Arrays.asList(attachment1, attachment2, attachment3));

    canvas = processThroughJson(canvas);

    //Collection shapes = canvas.getShapes();
    //assertEquals(3, shapes.size());
    //todo: uncomment with support for @XmlElementRefs.
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
//    todo: uncomment when you figure out the attachment stuff...
//    DataHandler backgroundImage = canvas.getBackgroundImage();
//    InputStream attachmentStream = backgroundImage.getInputStream();
//    ByteArrayOutputStream bgImageIn = new ByteArrayOutputStream();
//    int byteIn = attachmentStream.read();
//    while (byteIn > 0) {
//      bgImageIn.write(byteIn);
//      byteIn = attachmentStream.read();
//    }
//
//    assertTrue(Arrays.equals(swaRefBytes, bgImageIn.toByteArray()));
//
//    byte[] base64Attachment = canvas.getExplicitBase64Attachment();
//    assertNotNull(base64Attachment);
//    assertTrue(Arrays.equals(explicitBase64Bytes, base64Attachment));
//
//    Collection<CanvasAttachment> otherAttachments = canvas.getOtherAttachments();
//    assertEquals(3, otherAttachments.size());
//    Iterator<CanvasAttachment> attachmentsIt = otherAttachments.iterator();
//    int attachmentCount = 0;
//    while (attachmentsIt.hasNext()) {
//      CanvasAttachment otherAttachment = (CanvasAttachment) attachmentsIt.next();
//      byte[] otherAttachmentBytes = otherAttachment.getValue();
//      if (Arrays.equals(attachment1Bytes, otherAttachmentBytes)) {
//        attachmentCount++;
//      }
//      else if (Arrays.equals(attachment2Bytes, otherAttachmentBytes)) {
//        attachmentCount++;
//      }
//      else if (Arrays.equals(attachment3Bytes, otherAttachmentBytes)) {
//        attachmentCount++;
//      }
//      else {
//        fail("Unknown attachment.");
//      }
//    }
//    assertEquals(3, attachmentCount);

    //todo: test element ref to an attachment element
    //todo: test element refs of attachment elements.
  }


  protected <T> T processThroughJson(T object) throws Exception {
    JacksonJaxbJsonProvider provider = new JacksonJaxbJsonProvider();

    File in = File.createTempFile(object.getClass().getName() + "In", ".json", this.tempDir);
    File out = File.createTempFile(object.getClass().getName() + "Out", ".json", this.tempDir);
    FileOutputStream fos = new FileOutputStream(in);
    provider.writeTo(object, null, null, null, null, null, fos);
    fos.close();
    Process process = new ProcessBuilder(this.phpExe, this.exe.getAbsolutePath(), packageToModule(object.getClass().getName()), in.getAbsolutePath(), out.getAbsolutePath())
      .directory(this.exe.getParentFile())
      .redirectErrorStream(true)
      .start();
    BufferedReader procReader = new BufferedReader(new InputStreamReader(process.getInputStream()));
    String line = procReader.readLine();
    while (line != null) {
      System.out.println(line);
      line = procReader.readLine();
    }
    int exitStatus = process.waitFor();
    assertEquals("php process json failed.", 0, exitStatus);

    FileInputStream fis = new FileInputStream(out);
    return (T) provider.readFrom((Class<Object>) object.getClass(), object.getClass(), null, null, null, fis);
  }

  protected String packageToModule(String pckg) {
    if (pckg == null) {
      return null;
    }
    else {
      StringBuilder ns = new StringBuilder();
      for (StringTokenizer toks = new StringTokenizer(pckg, "."); toks.hasMoreTokens();) {
        String tok = toks.nextToken();
        ns.append(Character.toString(tok.charAt(0)).toUpperCase());
        if (tok.length() > 1) {
          ns.append(tok.substring(1));
        }
        if (toks.hasMoreTokens()) {
          ns.append("::");
        }
      }
      return ns.toString();
    }
  }

}
