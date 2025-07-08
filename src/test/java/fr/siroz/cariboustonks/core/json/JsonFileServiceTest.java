package fr.siroz.cariboustonks.core.json;

import com.google.gson.*;
import com.google.gson.reflect.TypeToken;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonToken;
import com.google.gson.stream.JsonWriter;
import fr.siroz.cariboustonks.core.json.adapters.ColorAdapter;
import fr.siroz.cariboustonks.core.json.adapters.InstantAdapter;
import fr.siroz.cariboustonks.util.colors.Color;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Instant;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;

class JsonFileServiceTest {

    private Gson gson;
    private JsonFileService jsonFileService;
    private Path testSaveFilePath;
    private Path readOnlyFilePath;

    @BeforeEach
    public void setup() throws IOException {
        this.gson = new GsonBuilder()
                .registerTypeAdapterFactory(new CustomTypeAdapterFactoryTest())
                .serializeNulls()
                .setPrettyPrinting()
                .create();

        this.jsonFileService = new JsonFileService(this.gson);

        this.testSaveFilePath = Paths.get("test_save.json");
        Files.deleteIfExists(this.testSaveFilePath);

        this.readOnlyFilePath = Paths.get("readonly_file.json");
        Files.deleteIfExists(this.readOnlyFilePath);
    }

    public static class CustomTypeAdapterFactoryTest implements TypeAdapterFactory {

        private final Map<Class<?>, TypeAdapter<?>> adapters = new HashMap<>();

        public CustomTypeAdapterFactoryTest() {
            TypeAdapter<Color> colorAdapter = new ColorAdapter();
            TypeAdapter<Instant> instantAdapter = new InstantAdapter();

            this.adapters.put(Color.class, colorAdapter);
            this.adapters.put(Instant.class, instantAdapter);

            this.adapters.put(SpecialObject.class, new SpecialObjectAdapter());
        }

        @SuppressWarnings("unchecked")
        @Override
        public <T> @Nullable TypeAdapter<T> create(Gson gson, @NotNull TypeToken<T> type) {
            TypeAdapter<?> adapter = this.adapters.get(type.getRawType());
            return adapter != null ? (TypeAdapter<T>) adapter : null;
        }
    }

    @Test
    void testLoad_success() throws IOException {
		Path testFilePath = Paths.get("test.json");

		String jsonData = "{\"name\": \"TestObject\"}";
		Files.write(testFilePath, jsonData.getBytes());

        MyObject result = jsonFileService.load(testFilePath, MyObject.class);

        assertNotNull(result, "Expected object to be loaded from file");
        assertEquals("TestObject", result.getName(), "Expected name to match the one in the file");
		Files.delete(testFilePath);
    }

    @Test
    void testLoad_object_success() throws IOException {
		Path testFilePath = Paths.get("test.json");

		String jsonData = "{\"name\": \"TestObject\"}";
		Files.write(testFilePath, jsonData.getBytes());
        Object result = jsonFileService.load(testFilePath, Object.class);

        assertNotNull(result, "Expected not null when invalid Object type, yea..");
		Files.delete(testFilePath);
    }

    @Test
    void testLoad_fileNotFound() {
        Path nonExistentPath = Paths.get("non_existent_file.json");
        MyObject result = jsonFileService.load(nonExistentPath, MyObject.class);

        assertNull(result, "Expected null when file is not found");
    }

    @Test
    void testLoad_invalidJson() throws IOException {
        Path invalidFilePath = Paths.get("invalid.json");
        String invalidJsonData = "{\"name\": \"TestObject\", }";  // Json malformé
        Files.write(invalidFilePath, invalidJsonData.getBytes());

        MyObject result = jsonFileService.load(invalidFilePath, MyObject.class);

        assertNull(result, "Expected null when JSON parsing fails");
		Files.delete(invalidFilePath);
    }

    @Test
    void testSaveAndLoad_validObject() {
        MyObject myObject = new MyObject("TestObject");

        jsonFileService.save(testSaveFilePath, myObject);

        assertTrue(Files.exists(testSaveFilePath), "Expected the file to be saved");

        try {
            String content = Files.readString(testSaveFilePath);
            assertTrue(content.contains("TestObject"), "Expected the file content to contain 'TestObject'");
        } catch (IOException ex) {
            fail("Failed to read the saved file");
        }
    }

    @Test
    void testSaveAndLoad_SpecialObject() throws IOException {
        Path tempFile = Files.createTempFile("test-special-object", ".json");

        AnotherClass anotherClass = new AnotherClass("TestName", 42.5);
        List<NestedClass> nestedList = Arrays.asList(
                new NestedClass(1, true),
                new NestedClass(2, false)
        );
        SpecialObject specialObject = new SpecialObject("Hello", 123, anotherClass, nestedList);

        jsonFileService.save(tempFile, specialObject);

        SpecialObject result = jsonFileService.load(tempFile, SpecialObject.class);

        assertNotNull(result);
        assertEquals(specialObject, result, "The deserialized object should match the original.");

        Files.delete(tempFile);
    }

    @Test
    void testSaveAndLoad_SpecialObjectWithNullValues() throws IOException {
        Path tempFile = Files.createTempFile("test-special-object-null", ".json");

        SpecialObject specialObject = new SpecialObject(null, 123, null, null);

        jsonFileService.save(tempFile, specialObject);

        SpecialObject result = jsonFileService.load(tempFile, SpecialObject.class);

        assertNotNull(result);
        assertNull(result.text, "Text should be null.");
        assertEquals(123, result.number);
        assertNull(result.anotherClass, "AnotherClass should be null.");
        assertNull(result.nestedList, "NestedList should be null.");

        Files.delete(tempFile);
    }

    @Test
    void testSaveAndLoad_SpecialObjectWithEmptyList() throws IOException {
        Path tempFile = Files.createTempFile("test-special-object-empty-list", ".json");

        AnotherClass anotherClass = new AnotherClass("TestName", 42.5);
        SpecialObject specialObject = new SpecialObject("Hello", 123, anotherClass, new ArrayList<>());

        jsonFileService.save(tempFile, specialObject);

        SpecialObject result = jsonFileService.load(tempFile, SpecialObject.class);

        assertNotNull(result);
        assertTrue(result.nestedList.isEmpty(), "The deserialized list should be empty.");
        assertNotSame(anotherClass.name, specialObject.text);

        Files.delete(tempFile);
    }

    @Test
    void testSave_collectionPrimitive_valid() {
        List<Integer> myObjects = Arrays.asList(1, 10, 555);

        jsonFileService.save(testSaveFilePath, myObjects);

        assertTrue(Files.exists(testSaveFilePath), "Expected the file to be saved");

        List<Integer> result = jsonFileService.loadList(testSaveFilePath, Integer.class);

        assertNotNull(result);
        assertFalse(result.isEmpty());
    }

    @Test
    void testLoad_collectionPrimitive_validResult() {
        List<Integer> myObjects = Arrays.asList(1, 10, 555);

        jsonFileService.save(testSaveFilePath, myObjects);

        List<Integer> result = jsonFileService.loadList(testSaveFilePath, Integer.class);

        assertEquals(3, result.size());
        assertEquals(1, result.get(0));
        assertEquals(10, result.get(1));
        assertEquals(555, result.get(2));
    }

    @Test
    void testSave_collectionObject_valid() {
        List<MyObject> myObjects = Arrays.asList(new MyObject("Object1"), new MyObject("Object2"));

        jsonFileService.save(testSaveFilePath, myObjects);

        assertTrue(Files.exists(testSaveFilePath), "Expected the file to be saved");

        List<MyObject> result = jsonFileService.loadList(testSaveFilePath, MyObject.class);

        assertNotNull(result);
        assertFalse(result.isEmpty());
    }

    @Test
    void testLoad_collectionObject_validResult() {
        List<MyObject> myObjects = Arrays.asList(new MyObject("Object1"), new MyObject("Object2"));

        jsonFileService.save(testSaveFilePath, myObjects);

        List<MyObject> result = jsonFileService.loadList(testSaveFilePath, MyObject.class);

        assertEquals(2, result.size());
        assertNotNull(result.get(0));
        assertNotNull(result.get(1));
        assertThrows(IndexOutOfBoundsException.class, () -> result.get(2));
        assertEquals("Object1", result.get(0).getName());
        assertEquals("Object2", result.get(1).getName());
    }

    @Test
    void testSave_objectWithList_valid() throws IOException {
        Path tempFile = Files.createTempFile("test-object-with-list", ".json");

        List<String> sampleList = Arrays.asList("Item1", "Item2", "Item3");
        ObjectWithList objectWithList = new ObjectWithList(sampleList);

        jsonFileService.save(tempFile, objectWithList);

        assertTrue(Files.exists(tempFile), "Expected the file to be saved");

        ObjectWithList result = jsonFileService.load(tempFile, ObjectWithList.class);

        assertNotNull(result);
        assertNotNull(result.list);
        assertFalse(result.list.isEmpty());

        Files.delete(tempFile);
    }

    @Test
    void testLoad_objectWithList_validResult() throws IOException {
        Path tempFile = Files.createTempFile("test-object-with-list", ".json");

        List<String> sampleList = Arrays.asList("Item1", "Item2", "Item3");
        ObjectWithList objectWithList = new ObjectWithList(sampleList);

        jsonFileService.save(tempFile, objectWithList);

        ObjectWithList result = jsonFileService.load(tempFile, ObjectWithList.class);

        assertNotNull(result);
        assertNotNull(result.list);
        assertEquals(3, result.list.size());
        assertNotNull(result.list.get(0));
        assertNotNull(result.list.get(1));
        assertNotNull(result.list.get(2));
        assertThrows(IndexOutOfBoundsException.class, () -> result.list.get(3));
        assertEquals("Item1", result.list.get(0));
        assertEquals("Item2", result.list.get(1));
        assertEquals("Item3", result.list.get(2));

        Files.delete(tempFile);
    }

    @Test
    void testSave_mapPrimitive_valid() {
        Map<String, Double> myMap = new HashMap<>();
        myMap.put("key1", 555.5D);
        myMap.put("key2", 555.555D);

        jsonFileService.save(testSaveFilePath, myMap);

        assertTrue(Files.exists(testSaveFilePath), "Expected the file to be saved");

        Map<String, Double> result = jsonFileService.loadMap(testSaveFilePath, String.class, Double.class);

        assertNotNull(result);
        assertFalse(result.isEmpty());
    }

    @Test
    void testLoad_mapPrimitive_validResult() {
        Map<String, Double> myMap = new HashMap<>();
        myMap.put("key1", 555.5D);
        myMap.put("key2", 555.555D);

        jsonFileService.save(testSaveFilePath, myMap);

        Map<String, Double> result = jsonFileService.loadMap(testSaveFilePath, String.class, Double.class);

        assertEquals(2, result.size());
        assertNotNull(result.get("key1"));
        assertNotNull(result.get("key2"));
        assertEquals(555.5D, result.get("key1"));
        assertEquals(555.555D, result.get("key2"));
    }

    @Test
    void testSave_mapObject_valid() {
        Map<String, MyObject> myMap = new HashMap<>();
        myMap.put("key1", new MyObject("Object1"));
        myMap.put("key2", new MyObject("Object2"));

        jsonFileService.save(testSaveFilePath, myMap);

        assertTrue(Files.exists(testSaveFilePath), "Expected the file to be saved");

        Map<String, MyObject> result = jsonFileService.loadMap(testSaveFilePath, String.class, MyObject.class);

        assertNotNull(result);
        assertFalse(result.isEmpty());
    }

    @Test
    void testLoad_mapObject_validResult() {
        Map<String, MyObject> myMap = new HashMap<>();
        MyObject myObject1 = new MyObject("Object1");
        MyObject myObject2 = new MyObject("Object2");
        myMap.put("key1", myObject1);
        myMap.put("key2", myObject2);

        jsonFileService.save(testSaveFilePath, myMap);

        Map<String, MyObject> result = jsonFileService.loadMap(testSaveFilePath, String.class, MyObject.class);

        assertEquals(2, result.size());
        assertNotNull(result.get("key1"));
        assertNotNull(result.get("key2"));
        assertNull(result.get("key"));
        assertNull(result.get("key3"));
        assertEquals(myObject1.getName(), result.get("key1").getName());
        assertEquals(myObject2.getName(), result.get("key2").getName());
    }

    @Test
    void testSave_objectWithMap_valid() throws IOException {
        Path tempFile = Files.createTempFile("test-object-with-map", ".json");

        Map<String, String> sampleMap = new HashMap<>();
        sampleMap.put("key1", "value1");
        sampleMap.put("key2", "value2");
        sampleMap.put("key3", "value3");
        ObjectWithMap objectWithMap = new ObjectWithMap(sampleMap);

        jsonFileService.save(tempFile, objectWithMap);

        assertTrue(Files.exists(tempFile), "Expected the file to be saved");

        ObjectWithMap result = jsonFileService.load(tempFile, ObjectWithMap.class);

        assertNotNull(result);
        assertNotNull(result.map);
        assertFalse(result.map.isEmpty());

        Files.delete(tempFile);
    }

    @Test
    void testLoad_objectWithMap_validResult() throws IOException {
        Path tempFile = Files.createTempFile("test-object-with-map", ".json");

        Map<String, String> sampleMap = new HashMap<>();
        sampleMap.put("key1", "value1");
        sampleMap.put("key2", "value2");
        sampleMap.put("key3", "value3");
        ObjectWithMap objectWithMap = new ObjectWithMap(sampleMap);

        jsonFileService.save(tempFile, objectWithMap);

        ObjectWithMap result = jsonFileService.load(tempFile, ObjectWithMap.class);

        assertNotNull(result);
        assertNotNull(result.map);
        assertEquals(3, result.map.size());
        assertNotNull(result.map.get("key1"));
        assertNotNull(result.map.get("key2"));
        assertNotNull(result.map.get("key3"));
        assertNull(result.map.get("key4"));
        assertEquals("value1", result.map.get("key1"));
        assertEquals("value2", result.map.get("key2"));
        assertEquals("value3", result.map.get("key3"));

        Files.delete(tempFile);
    }

    @Test
    void testSaveAndLoad_objectWithEmptyList() throws IOException {
        Path tempFile = Files.createTempFile("test-object-with-empty-list", ".json");

        ObjectWithList objectWithList = new ObjectWithList(new ArrayList<>());

        jsonFileService.save(tempFile, objectWithList);
        ObjectWithList result = jsonFileService.load(tempFile, ObjectWithList.class);

        assertNotNull(result);
        assertTrue(result.list.isEmpty(), "The deserialized list should be empty.");

        Files.delete(tempFile);
    }

    @Test
    void testSaveAndLoad_objectWithEmptyMap() throws IOException {
        Path tempFile = Files.createTempFile("test-object-with-empty-map", ".json");

        ObjectWithMap objectWithMap = new ObjectWithMap(new HashMap<>());

        jsonFileService.save(tempFile, objectWithMap);
        ObjectWithMap result = jsonFileService.load(tempFile, ObjectWithMap.class);

        assertNotNull(result);
        assertTrue(result.map.isEmpty(), "The deserialized map should be empty.");

        Files.delete(tempFile);
    }

    @Test
    void testSaveAndLoad_objectWithListContainingNull() throws IOException {
        Path tempFile = Files.createTempFile("test-object-with-list-containing-null", ".json");

        List<String> sampleList = Arrays.asList("Item1", null, "Item3");
        ObjectWithList objectWithList = new ObjectWithList(sampleList);

        jsonFileService.save(tempFile, objectWithList);

        ObjectWithList result = jsonFileService.load(tempFile, ObjectWithList.class);

        assertNotNull(result);
        assertEquals(objectWithList.list, result.list, "The deserialized list should match, including null values.");

        Files.delete(tempFile);
    }

    @Test
    void testSaveAndLoad_objectWithMapContainingNull() throws IOException {
        Path tempFile = Files.createTempFile("test-object-with-map-containing-null", ".json");

        Map<String, String> sampleMap = new HashMap<>();
        sampleMap.put("key1", "value1");
        sampleMap.put("key2", null);
        ObjectWithMap objectWithMap = new ObjectWithMap(sampleMap);

        jsonFileService.save(tempFile, objectWithMap);

        ObjectWithMap result = jsonFileService.load(tempFile, ObjectWithMap.class);

        assertNotNull(result);
        assertEquals(objectWithMap.map, result.map, "The deserialized map should match, including null values.");

        Files.delete(tempFile);
    }

    @Test
    void testSave_IOException() throws IOException {
        // Simule une erreur lors de l'écriture en créant un fichier en lecture seule
        Files.createFile(readOnlyFilePath);
        boolean b = readOnlyFilePath.toFile().setReadOnly();
        MyObject myObject = new MyObject("TestObject::" + b);

        assertThrows(Exception.class, () -> {
            jsonFileService.save(readOnlyFilePath, myObject);
        });
    }

    @Test
    void testLoadList_invalidJson_throwsJsonProcessingException() throws IOException {
        String invalidJson = "[\"item1\", \"item2\", \"value\": 20]";
        Path tempFile = Files.createTempFile("test-loadList-invalid", ".json");
        Files.write(tempFile, invalidJson.getBytes());

        assertThrows(JsonProcessingException.class, () -> jsonFileService.loadList(tempFile, String.class));

        Files.delete(tempFile);
    }

    @Test
    void testLoadList_invalidType_throwsJsonProcessingException() throws IOException {
        String json = "[\"item1\", \"item2\"]";
        Path tempFile = Files.createTempFile("test-loadList-type", ".json");
        Files.write(tempFile, json.getBytes());

        // String != Integer
        assertThrows(JsonProcessingException.class, () -> jsonFileService.loadList(tempFile, Integer.class));

        Files.delete(tempFile);
    }

    @Test
    void testLoadMap_invalidJson_throwsJsonProcessingException() throws IOException {
        String invalidJson = "{\"key1\": \"value1\", \"key2\": \"value2\",}";
        Path tempFile = Files.createTempFile("test-loadMap-invalid", ".json");
        Files.write(tempFile, invalidJson.getBytes());

        assertThrows(JsonProcessingException.class, () -> jsonFileService.loadMap(tempFile, String.class, String.class));

        Files.delete(tempFile);
    }

    @Test
    void testLoadMap_invalidType_throwsJsonProcessingException() throws IOException {
        String json = "{\"key1\": \"value1\", \"key2\": \"value2\"}";
        Path tempFile = Files.createTempFile("test-loadMap-type", ".json");
        Files.write(tempFile, json.getBytes());

        // Integer != String
        assertThrows(JsonProcessingException.class, () -> jsonFileService.loadMap(tempFile, Integer.class, String.class));

        Files.delete(tempFile);
    }

    @Test
    void testInstantSerialization() {
        Instant now = Instant.now();
        String json = gson.toJson(now);
        Instant deserialized = gson.fromJson(json, Instant.class);

        assertNotNull(json);
        assertEquals(now, deserialized, "The deserialized instant should match the original.");
    }

    @Test
    void testColorSerialization() {
        Color color = new Color(255, 0, 0);
        String json = gson.toJson(color);
        Color deserialized = gson.fromJson(json, Color.class);

        assertNotNull(json);
        assertEquals(color, deserialized, "The deserialized color should match the original.");
    }

    @Test
    void testInstantDeserialization_withInvalidFormat() {
        String invalidJson = "\"2021-15-40T25:61:00Z\""; // Invalide format
        assertThrows(JsonParseException.class, () -> gson.fromJson(invalidJson, Instant.class),
                "Expected JsonParseException due to invalid Instant format");
    }

    @Test
    void testColorDeserialization_withInvalidHex() {
        String invalidJson = "\"#zzzzzz\""; // Invalide hex

        Color color = gson.fromJson(invalidJson, Color.class);

        assertNotNull(color);
        assertEquals(Color.DEFAULT, color);
    }

    @Test
    void testInstantSerialization_withEdgeDate() {
        Instant minInstant = Instant.MIN;  // Earliest
        String json = gson.toJson(minInstant);
        Instant deserialized = gson.fromJson(json, Instant.class);

        assertNotNull(json);
        assertEquals(minInstant, deserialized, "The deserialized Instant should match the minimum valid Instant.");
    }

    @Test
    void testColorSerialization_withEdgeColor() {
        Color transparentColor = new Color(0, 0, 0, 0); // Transparent
        String json = gson.toJson(transparentColor);
        Color deserialized = gson.fromJson(json, Color.class);

        assertNotNull(json);
        assertEquals(transparentColor, deserialized, "The deserialized transparent color should match the original.");
    }

    @Test
    void testSaveAndLoad_objectWithInstant() throws IOException {
        Path tempFile = Files.createTempFile("test-object-with-instant", ".json");
        MyInstantObject myInstantObject = new MyInstantObject("TestInstantObject", Instant.now());

        jsonFileService.save(tempFile, myInstantObject);

        MyInstantObject result = jsonFileService.load(tempFile, MyInstantObject.class);

        assertNotNull(result);
        assertEquals(myInstantObject.getName(), result.getName(), "The deserialized object's name should match.");
        assertEquals(myInstantObject.getTimestamp(), result.getTimestamp(), "The deserialized object's timestamp should match.");

        Files.delete(tempFile);
    }

    @Test
    void testSaveAndLoad_objectWithColor() throws IOException {
        Path tempFile = Files.createTempFile("test-object-with-color", ".json");
        MyColorObject myColorObject = new MyColorObject(new Color(255, 255, 0));

        jsonFileService.save(tempFile, myColorObject);

        MyColorObject result = jsonFileService.load(tempFile, MyColorObject.class);

        assertNotNull(result);
        assertEquals(myColorObject.getColor(), result.getColor(), "The deserialized color should match.");

        Files.delete(tempFile);
    }

    @SuppressWarnings("ClassCanBeRecord")
    public static class MyObject {
        public final String name;

        public MyObject(String name) {
            this.name = name;
        }

        public String getName() {
            return name;
        }
    }

    @SuppressWarnings("ClassCanBeRecord")
    public static class ObjectWithList {
        public final List<String> list;

        public ObjectWithList(List<String> list) {
            this.list = list;
        }
    }

    @SuppressWarnings("ClassCanBeRecord")
    public static class ObjectWithMap {
        public final Map<String, String> map;

        public ObjectWithMap(Map<String, String> map) {
            this.map = map;
        }
    }

    @SuppressWarnings("ClassCanBeRecord")
    public static class MyColorObject {
        private final Color color;

        public MyColorObject(Color color) {
            this.color = color;
        }

        public Color getColor() {
            return color;
        }
    }

    @SuppressWarnings("ClassCanBeRecord")
    public static class MyInstantObject {
        private final String name;
        private final Instant timestamp;

        public MyInstantObject(String name, Instant timestamp) {
            this.name = name;
            this.timestamp = timestamp;
        }

        public String getName() {
            return name;
        }

        public Instant getTimestamp() {
            return timestamp;
        }
    }

    @SuppressWarnings("ClassCanBeRecord")
    public static class AnotherClass {
        public final String name;
        public final double value;

        public AnotherClass(String name, double value) {
            this.name = name;
            this.value = value;
        }

        @Override
        public boolean equals(Object obj) {
            if (this == obj) return true;
            if (obj == null || getClass() != obj.getClass()) return false;
            AnotherClass that = (AnotherClass) obj;
            return Double.compare(that.value, value) == 0 &&
                    Objects.equals(name, that.name);
        }
    }

    @SuppressWarnings("ClassCanBeRecord")
    public static class NestedClass {
        public final int id;
        public final boolean active;

        public NestedClass(int id, boolean active) {
            this.id = id;
            this.active = active;
        }

        @Override
        public boolean equals(Object obj) {
            if (this == obj) return true;
            if (obj == null || getClass() != obj.getClass()) return false;
            NestedClass that = (NestedClass) obj;
            return id == that.id && active == that.active;
        }
    }

    @SuppressWarnings("ClassCanBeRecord")
    public static class SpecialObject {
        public final String text;
        public final int number;
        public final AnotherClass anotherClass;
        public final List<NestedClass> nestedList;

        public SpecialObject(String text, int number, AnotherClass anotherClass, List<NestedClass> nestedList) {
            this.text = text;
            this.number = number;
            this.anotherClass = anotherClass;
            this.nestedList = nestedList;
        }

        @Override
        public boolean equals(Object obj) {
            if (this == obj) return true;
            if (obj == null || getClass() != obj.getClass()) return false;
            SpecialObject that = (SpecialObject) obj;
            return number == that.number &&
                    Objects.equals(text, that.text) &&
                    Objects.equals(anotherClass, that.anotherClass) &&
                    Objects.equals(nestedList, that.nestedList);
        }
    }

    public static class SpecialObjectAdapter extends TypeAdapter<SpecialObject> {
        @Override
        public void write(JsonWriter out, SpecialObject value) throws IOException {
            out.beginObject();
            out.name("text").value(value.text);
            out.name("number").value(value.number);

            if (value.anotherClass != null) {
                out.name("anotherClass");
                out.beginObject();
                out.name("name").value(value.anotherClass.name);
                out.name("value").value(value.anotherClass.value);
                out.endObject();
            } else {
                out.name("anotherClass").nullValue();
            }

            if (value.anotherClass != null) {
                out.name("anotherClass");
                out.beginObject();
                out.name("name").value(value.anotherClass.name);
                out.name("value").value(value.anotherClass.value);
                out.endObject();
            } else {
                out.name("anotherClass").nullValue();
            }

            if (value.nestedList != null) {
                out.name("nestedList");
                out.beginArray();
                for (NestedClass nested : value.nestedList) {
                    out.beginObject();
                    out.name("id").value(nested.id);
                    out.name("active").value(nested.active);
                    out.endObject();
                }
                out.endArray();
            } else {
                out.name("nestedList").nullValue();
            }

            out.endObject();
        }

        @Override
        public SpecialObject read(JsonReader in) throws IOException {
            String text = null;
            int number = 0;
            AnotherClass anotherClass = null;
            List<NestedClass> nestedList = null;

            in.beginObject();
            while (in.hasNext()) {
                switch (in.nextName()) {
                    case "text":
                        if (in.peek() == JsonToken.NULL) {
                            in.nextNull();
                            text = null;
                        } else {
                            text = in.nextString();
                        }
                        break;
                    case "number":
                        number = in.nextInt();
                        break;
                    case "anotherClass":
                        if (in.peek() == JsonToken.NULL) {
                            in.nextNull();
                            anotherClass = null;
                        } else {
                            in.beginObject();
                            String name = null;
                            double value = 0.0;
                            while (in.hasNext()) {
                                switch (in.nextName()) {
                                    case "name":
                                        if (in.peek() == JsonToken.NULL) {
                                            in.nextNull();
                                            name = null;
                                        } else {
                                            name = in.nextString();
                                        }
                                        break;
                                    case "value":
                                        value = in.nextDouble();
                                        break;
                                }
                            }
                            anotherClass = new AnotherClass(name, value);
                            in.endObject();
                        }
                        break;
                    case "nestedList":
                        if (in.peek() == JsonToken.NULL) {
                            in.nextNull();
                            nestedList = null;
                        } else {
                            nestedList = new ArrayList<>();
                            in.beginArray();
                            while (in.hasNext()) {
                                in.beginObject();
                                int id = 0;
                                boolean active = false;
                                while (in.hasNext()) {
                                    switch (in.nextName()) {
                                        case "id":
                                            id = in.nextInt();
                                            break;
                                        case "active":
                                            active = in.nextBoolean();
                                            break;
                                    }
                                }
                                nestedList.add(new NestedClass(id, active));
                                in.endObject();
                            }
                            in.endArray();
                        }
                        break;
                }
            }
            in.endObject();
            return new SpecialObject(text, number, anotherClass, nestedList);
        }
    }
}
