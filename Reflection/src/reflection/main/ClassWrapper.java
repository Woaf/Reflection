/*
 * Copyright (C) 2019 Balint Fazekas
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package reflection.main;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import lombok.Data;

/**
 *
 * @author balin
 */
@Data
public class ClassWrapper {
    
    private String name;
    
    private List<Constructor<?>> class_constructors = new ArrayList<>();
    private List<Method> class_methods = new ArrayList<>();
    private List<Field> class_fields = new ArrayList<>();
    private Set<String> class_method_names = new HashSet<>();
    
    private boolean isEvolutionary = false;

    public ClassWrapper() {
    }

    public List<Constructor<?>> getClass_constructors() {
        return class_constructors;
    }

    public void setClass_constructors(List<Constructor<?>> class_constructors) {
        this.class_constructors = class_constructors;
    }

    public List<Method> getClass_methods() {
        return class_methods;
    }
    
    public void filterMethodName()
    {
        Set<String> tempNameSet = new HashSet<>();
        for(String m : class_method_names)
        {
            if(!m.equals("setId_")
                    && !m.equals("getId_")
                    && !m.equals("canEqual")
                    && !m.equals("equals")
                    && !m.equals("toString")
                    && !m.equals("hashCode"))
            {
                tempNameSet.add(m);
            }
        }
        class_method_names = tempNameSet;
    }

    public void setClass_methods(List<Method> class_methods) {
        this.class_methods = class_methods;
        for(Method m : class_methods)
        {
            this.class_method_names.add(m.getName());
        }
    }

    public List<Field> getClass_fields() {
        return class_fields;
    }

    public void setClass_fields(List<Field> class_fields) {
        this.class_fields = class_fields;
    }
    
    public boolean isEvolutionary()
    {
        return isEvolutionary;
    }
    
    public void setEvolutionary(boolean evo)
    {
        isEvolutionary = evo;
    }
    
}
