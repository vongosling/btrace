/*
 * Copyright 2008-2010 Sun Microsystems, Inc.  All Rights Reserved.
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS FILE HEADER.
 *
 * This code is free software; you can redistribute it and/or modify it
 * under the terms of the GNU General Public License version 2 only, as
 * published by the Free Software Foundation.  Sun designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Sun in the LICENSE file that accompanied this code.
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
 * Please contact Sun Microsystems, Inc., 4150 Network Circle, Santa Clara,
 * CA 95054 USA or visit www.sun.com if you need additional information or
 * have any questions.
 */

package traces.issues;

import net.java.btrace.annotations.*;
import net.java.btrace.api.types.AnyType;

@BTrace public class BTRACE87 {
    @OnMethod(
        clazz="/.*\\.BTRACE87/", 
        method="/.*/",
        location=@Location(value=Kind.CALL, clazz="/.*/", method="/.*/") 
    )
    public static void o(@Self Object self, @ProbeMethodName String pmn, AnyType[] args) { // all calls to methods
        // self - this for the method call
        // pmn - textual representation of the method
        // contents of args array:
        // [0]..[n] - original method call arguments
        
        // whatever
    }
}