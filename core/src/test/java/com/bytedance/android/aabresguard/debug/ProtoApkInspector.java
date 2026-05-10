package com.bytedance.android.aabresguard.debug;

import com.android.aapt.Resources;
import com.google.protobuf.ByteString;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Enumeration;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

public final class ProtoApkInspector {

    public static void main(String[] args) throws Exception {
        if (args.length == 0) {
            System.err.println("Usage: ProtoApkInspector <proto-apk>");
            System.exit(1);
        }

        Path apkPath = Paths.get(args[0]);
        List<String> issues = inspect(apkPath);
        if (issues.isEmpty()) {
            System.out.println("No obvious proto issues found in " + apkPath);
            return;
        }
        issues.forEach(System.out::println);
        System.exit(2);
    }

    public static List<String> inspect(Path apkPath) throws IOException {
        List<String> issues = new ArrayList<>();
        try (ZipFile zipFile = new ZipFile(apkPath.toFile())) {
            inspectResourcesTable(zipFile, issues);
            inspectXmlEntries(zipFile, issues);
        }
        issues.sort(Comparator.naturalOrder());
        return issues;
    }

    private static void inspectResourcesTable(ZipFile zipFile, List<String> issues) throws IOException {
        ZipEntry entry = zipFile.getEntry("resources.pb");
        if (entry == null) {
            issues.add("[resources.pb] missing");
            return;
        }
        Resources.ResourceTable table = Resources.ResourceTable.parseFrom(readAllBytes(zipFile, entry));
        for (Resources.Package pkg : table.getPackageList()) {
            for (Resources.Type type : pkg.getTypeList()) {
                for (Resources.Entry resEntry : type.getEntryList()) {
                    String prefix = String.format(
                            "[resources.pb] %s:%s/%s",
                            pkg.getPackageName(),
                            type.getName(),
                            resEntry.getName()
                    );
                    for (int i = 0; i < resEntry.getConfigValueCount(); i++) {
                        inspectValue(prefix + " config[" + i + "]", resEntry.getConfigValue(i).getValue(), issues);
                    }
                }
            }
        }
    }

    private static void inspectXmlEntries(ZipFile zipFile, List<String> issues) throws IOException {
        Enumeration<? extends ZipEntry> entries = zipFile.entries();
        while (entries.hasMoreElements()) {
            ZipEntry entry = entries.nextElement();
            String name = entry.getName();
            if (!(name.equals("AndroidManifest.xml") || name.startsWith("res/")) || !name.endsWith(".xml")) {
                continue;
            }
            try {
                Resources.XmlNode xmlNode = Resources.XmlNode.parseFrom(readAllBytes(zipFile, entry));
                inspectXmlNode(name, xmlNode, issues);
            } catch (Exception e) {
                issues.add("[" + name + "] parse error: " + e.getClass().getSimpleName() + ": " + e.getMessage());
            }
        }
    }

    private static void inspectXmlNode(String location, Resources.XmlNode node, List<String> issues) {
        if (node.getNodeCase() == Resources.XmlNode.NodeCase.NODE_NOT_SET) {
            issues.add("[" + location + "] node case not set");
            return;
        }
        if (node.hasElement()) {
            Resources.XmlElement element = node.getElement();
            for (int i = 0; i < element.getAttributeCount(); i++) {
                Resources.XmlAttribute attribute = element.getAttribute(i);
                if (attribute.hasCompiledItem()) {
                    Resources.Item item = attribute.getCompiledItem();
                    if (item.getValueCase() == Resources.Item.ValueCase.VALUE_NOT_SET) {
                        issues.add(String.format(
                                "[%s] attr %s compiledItem value not set",
                                location,
                                qualifiedName(attribute.getNamespaceUri(), attribute.getName())
                        ));
                    }
                }
            }
            for (int i = 0; i < element.getChildCount(); i++) {
                inspectXmlNode(location + "/" + element.getName() + "#" + i, element.getChild(i), issues);
            }
        }
    }

    private static void inspectValue(String location, Resources.Value value, List<String> issues) {
        if (value.getValueCase() == Resources.Value.ValueCase.VALUE_NOT_SET) {
            issues.add(location + " value case not set");
            return;
        }
        if (value.hasItem()) {
            inspectItem(location, value.getItem(), issues);
        }
        if (value.hasCompoundValue()) {
            Resources.CompoundValue compoundValue = value.getCompoundValue();
            if (compoundValue.getValueCase() == Resources.CompoundValue.ValueCase.VALUE_NOT_SET) {
                issues.add(location + " compound value not set");
            }
            switch (compoundValue.getValueCase()) {
                case STYLE:
                    for (int i = 0; i < compoundValue.getStyle().getEntryCount(); i++) {
                        inspectItem(location + " style[" + i + "]", compoundValue.getStyle().getEntry(i).getItem(), issues);
                    }
                    break;
                case STYLEABLE:
                case ARRAY:
                case ATTR:
                case PLURAL:
                case VALUE_NOT_SET:
                default:
                    break;
            }
        }
    }

    private static void inspectItem(String location, Resources.Item item, List<String> issues) {
        if (item.getValueCase() == Resources.Item.ValueCase.VALUE_NOT_SET) {
            issues.add(location + " item value not set");
        }
    }

    private static String qualifiedName(String namespace, String name) {
        return namespace == null || namespace.isEmpty() ? name : namespace + ":" + name;
    }

    private static byte[] readAllBytes(ZipFile zipFile, ZipEntry entry) throws IOException {
        try (InputStream inputStream = zipFile.getInputStream(entry)) {
            return ByteString.readFrom(inputStream).toByteArray();
        }
    }
}
