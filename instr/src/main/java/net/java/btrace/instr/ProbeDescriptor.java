/*
 * Copyright (c) 2007, 2011, Oracle and/or its affiliates. All rights reserved.
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS FILE HEADER.
 *
 * This code is free software; you can redistribute it and/or modify it
 * under the terms of the GNU General Public License version 2 only, as
 * published by the Free Software Foundation.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the LICENSE file that accompanied this code.
 *
 * This code is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General Public License
 * version 2 for more details (a copy is included in the LICENSE file that
 * accompanied this code).
 *
 * You should have received a copy of the GNU General Public License version
 * 2 along with this work; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin St, Fifth Floor, Boston, MA 02110-1301 USA.
 *
 * Please contact Oracle, 500 Oracle Parkway, Redwood Shores, CA 94065 USA
 * or visit www.oracle.com if you need additional information or have any
 * questions.
 */

package net.java.btrace.instr;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement(name="btrace-probes")
public class ProbeDescriptor {
   private String namespace;
   private Collection<OnProbe> probes;
   private Map<String, OnProbe> nameToProbeMap;

   public ProbeDescriptor() {
       nameToProbeMap = new HashMap<String, OnProbe>();
   }

   @XmlAttribute(name="namespace")
   public String getNamespace() {
       return namespace;
   }

   public void setNamespace(String namespace) {
       this.namespace = namespace;
   }

   @XmlElement(name="probe")
   public Collection<OnProbe> getProbes() {
       return probes;
   }

   public void setProbes(Collection<OnProbe> probes) {
       this.probes = probes;
       for (OnProbe op: probes) {
           nameToProbeMap.put(op.getName(), op);
       }
   }

   public OnProbe findProbe(String name) {
       return nameToProbeMap.get(name);
   }
}