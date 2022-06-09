package com.pesco.operator.hadoop;

import com.pesco.operator.common.crd.EnvConfig;
import com.pesco.operator.common.type.ConfigType;
import com.pesco.operator.hadoop.config.crd.HadoopConfigSpec;
import com.pesco.operator.hadoop.config.dependent.HadoopConfigConfigMapResource;
import org.junit.jupiter.api.Test;

import java.util.List;

public class TestXml {

    @Test
    public void testXml() {
        String content = "<configuration>\n <property>\n   <name>dfs.permissions.superusergroup</name>\n   <value>hdfs</value>\n </property>\n <property>\n  <name>dfs.replication</name>\n  <value>1</value>\n </property>\n</configuration>";
        HadoopConfigSpec spec = new HadoopConfigSpec();
        spec.setContent(content);
        spec.setFileName("hdfs-site.xml");
        spec.setType(ConfigType.hadoop_xml);
        spec.setOverwrites(List.of(new EnvConfig("dfs.replication", "2"), new EnvConfig("xxx.ttt", "xxxx")));

        String realContent = HadoopConfigConfigMapResource.getContent(spec);
        System.out.println(realContent);
    }

}
