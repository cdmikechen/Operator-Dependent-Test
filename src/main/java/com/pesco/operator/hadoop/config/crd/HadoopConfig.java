package com.pesco.operator.hadoop.config.crd;

import io.fabric8.kubernetes.api.model.Namespaced;
import io.fabric8.kubernetes.client.CustomResource;
import io.fabric8.kubernetes.model.annotation.*;
import io.quarkus.runtime.annotations.RegisterForReflection;

@Group("com.pesco.operator")
@Kind("HadoopConfig")
@Plural("hadoopconfigs")
@Singular("hadoopconfig")
@ShortNames("hdc")
@Version("v1")
@RegisterForReflection
public class HadoopConfig extends CustomResource<HadoopConfigSpec, HadoopConfigStatus> implements Namespaced {
}
