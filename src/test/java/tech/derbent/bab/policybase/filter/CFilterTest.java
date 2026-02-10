package tech.derbent.bab.policybase.filter;

import java.util.List;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

public class CFilterTest {

@Test
public void testCSVSerialization() {
= new CFilterCSV();
ame("Test");
abled(true);
eNumber(2);
eNumber(100);
g json = filter.toJson();
otNull(json);
.contains("CSV"));
.contains("Test"));
= CFilterCSV.createFromJson(json);
ame());
eNumber());
eNumber());
}

@Test
public void testJSONSerialization() {
 filter = new CFilterJSON();
ame("Sensor");
ts(50);
g json = filter.toJson();
otNull(json);
 restored = CFilterJSON.createFromJson(json);
sor", restored.getName());
void testXMLSerialization() {
= new CFilterXML();
ame("SOAP");
");
ts(100);
g json = filter.toJson();
otNull(json);
= CFilterXML.createFromJson(json);
ame());
restored.getXpathQuery());
}
}
