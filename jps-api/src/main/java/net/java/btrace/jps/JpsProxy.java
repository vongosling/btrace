/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2007 Sun Microsystems, Inc. All rights reserved.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common
 * Development and Distribution License("CDDL") (collectively, the
 * "License"). You may not use this file except in compliance with the
 * License. You can obtain a copy of the License at
 * http://www.netbeans.org/cddl-gplv2.html
 * or nbbuild/licenses/CDDL-GPL-2-CP. See the License for the
 * specific language governing permissions and limitations under the
 * License.  When distributing the software, include this License Header
 * Notice in each file and include the License file at
 * nbbuild/licenses/CDDL-GPL-2-CP.  Sun designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Sun in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the
 * License Header, with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * Contributor(s):
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
 *
 * If you wish your version of this file to be governed by only the CDDL
 * or only the GPL Version 2, indicate your decision by adding
 * "[Contributor] elects to include this software in this distribution
 * under the [CDDL or GPL Version 2] license." If you do not indicate a
 * single choice of license, a recipient has the option to distribute
 * your version of this file under either the CDDL, the GPL Version 2 or
 * to extend the choice of license to its licensees as provided above.
 * However, if you add GPL Version 2 code and therefore, elected the GPL
 * Version 2 license, then the option applies only if the new code is
 * made subject to such option by the copyright holder.
 */

package net.java.btrace.jps;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.logging.Level;
import sun.jvmstat.monitor.*;
import java.lang.management.ManagementFactory;
import java.net.URISyntaxException;
import java.util.HashSet;
import java.util.Set;
import java.util.logging.Logger;


/**
 * This class is based on "jvmps" class from jvmps 2.0 written by Brian Doherty.
 * It provides functionality to identify all the JVMs currently running on the local machine.
 * Comments starting with //// are original comments from Brian.
 *
 * @author Tomas Hurka
 * @author Misha Dmitriev
 */
public class JpsProxy {
    private static final Logger LOGGER = Logger.getLogger(JpsProxy.class.getName());

    public static Set<JpsVM> getRunningVMs() {
        String hostname = null;
        Set<JpsVM> vms = new HashSet<JpsVM>();

        try {
            HostIdentifier hostId = new HostIdentifier(hostname);
            MonitoredHost monitoredHost = MonitoredHost.getMonitoredHost(hostId);
            String selfName = ManagementFactory.getRuntimeMXBean().getName();

            //// get the list active VMs on the specified host.
            Set jvms = monitoredHost.activeVms();

            if (jvms.isEmpty()) {
                return null;
            }

            for (Object jvm : jvms) {
                int lvmid = ((Integer) jvm);

                if (selfName.startsWith(lvmid + "@")) { // myself

                    continue;
                }

                VmIdentifier id = null;
                MonitoredVm vm = null;
                String uriString = "//" + lvmid + "?mode=r"; // NOI18N

                try {
                    id = new VmIdentifier(uriString);
                    vm = monitoredHost.getMonitoredVm(id, 0);
                } catch (URISyntaxException e) {
                    //// this error should not occur as we are creating our own VMIdentifiers above based on a validated HostIdentifier.
                    //// This would be an unexpected condition.
                    LOGGER.log(Level.WARNING, "Detected malformed VM Identifier: {0}; ignored", uriString); // NOI18N

                    continue;
                } catch (MonitorException e) {
                    LOGGER.log(Level.WARNING, "VM {0} has already terminated", lvmid);

                    //// it's possible that from the time we acquired the list of available jvms that a jvm has terminated. Therefore, it is
                    //// best just to ignore this error.
                    continue;
                } catch (Exception e) {
                    //// certain types of errors, such as access acceptions, can be encountered when attaching to a jvm.
                    //// These are reported as exceptions, not as some subclass of security exception.
                    LOGGER.log(Level.WARNING, "VM {0} threw exception: {1}", new Object[]{String.valueOf(lvmid), e}); // NOI18N

                    continue;
                }

                if (!isAttachable(vm)) {
                    monitoredHost.detach(vm);

                    continue;
                }

                String mainClass = MonitoredVmUtil.mainClass(vm, true);
                String mainArgs = MonitoredVmUtil.mainArgs(vm);
                String vmArgs = MonitoredVmUtil.jvmArgs(vm);
                String vmFlags = MonitoredVmUtil.jvmFlags(vm);

                monitoredHost.detach(vm);

                JpsVM rvm = new JpsVM(lvmid, vmFlags, vmArgs, mainClass, mainArgs);
                vms.add(rvm);
            }
        } catch (MonitorException e) {
            String report = "in jvmps, got MonitorException"; // NOI18N

            if (e.getMessage() != null) {
                report += (" with message + " + e.getMessage()); // NOI18N
            }

            LOGGER.warning(report);

            return null;
        } catch (URISyntaxException e) {
            LOGGER.log(Level.WARNING, "in jvmps, got malformed Host Identifier: {0}", hostname); // NOI18N

            return null;
        }

        return vms;
    }

    // invoke MonitoredVmUtil.isAttachable(MonitoredVm vm) using reflection (JDK 6 only code)
    private static Method monitoredVmUtil_isAttachable;

    static {
        try {
            monitoredVmUtil_isAttachable = MonitoredVmUtil.class.getMethod("isAttachable",new Class[]{MonitoredVm.class}); // NOI18N
        } catch (SecurityException ex) {
            ex.printStackTrace();
        } catch (NoSuchMethodException ex) {
            ex.printStackTrace();
        }
    }

    private static boolean isAttachable(MonitoredVm vm) {
        Object ret;
        try {
            ret = monitoredVmUtil_isAttachable.invoke(null, new Object[] {vm});
            if (ret instanceof Boolean) {
                return ((Boolean)ret);
            }
        } catch (IllegalArgumentException ex) {
            ex.printStackTrace();
        } catch (IllegalAccessException ex) {
            ex.printStackTrace();
        } catch (InvocationTargetException ex) {
            ex.printStackTrace();
        }
        return false;
    }
}
