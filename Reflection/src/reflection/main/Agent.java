/*
 * Copyright (C) 2019 balin
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

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Random;
import java.util.stream.Collectors;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;
import lombok.Data;

/**
 *
 * @author woaf
 */
@Data
@Entity
@Table(schema = "WOAF", name = "AGENT")
public class Agent {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private int id_;
    private List<ClassWrapper> knownClasses_ = new ArrayList<>();
    private String localClassOfInterest = "";
    private String localTypeOfInterest = null;

    public Agent() {
    }

    public void addWrappers(List<ClassWrapper> cw) {
        knownClasses_.addAll(cw);
    }

    public void updateClassWrapperWithName(String name, Method method) {
        Optional<ClassWrapper> cw = knownClasses_.stream().filter(kw -> kw.getName().equals(name)).findFirst();
        if (cw.get() != null) {
            cw.get().getClass_method_names().add(method.getName());
            cw.get().filterMethodName();
            if (!cw.get().getClass_methods().contains(method)) {
                cw.get().getClass_methods().add(method);
                knownClasses_.removeIf(c -> c.getName().equals(name));
                knownClasses_.add(cw.get());
            }
            
        }

    }

    private void pealConstructorName(String constructorName) {
        String[] parts = constructorName.split(" ");
        String[] constructorParts = parts[1].split("\\(");
        localClassOfInterest = constructorParts[0];
    }

    public void exposeObject() {
        List<Object> evoObj = knownClasses_.stream()
                .filter(c -> c.isEvolutionary())
                .collect(Collectors.toList());

        Random rnd = new Random();
        int randomIndex = rnd.nextInt(evoObj.size());
        ClassWrapper o = (ClassWrapper) evoObj.get(randomIndex);

        // TODO: explanation
        int constructorIndex = rnd.nextInt(o.getClass_constructors().size());
        pealConstructorName(o.getClass_constructors().get(constructorIndex).toString());
        //System.out.println("Class name: " + localClassOfInterest);
        //System.out.println("Constructor index: " + constructorIndex);
        int paramListSize = o.getClass_constructors()
                .get(constructorIndex)
                .getParameters().length;

        int parameterIndex = 0;
        if (paramListSize > 0) {
            parameterIndex = rnd.nextInt(
                    o.getClass_constructors()
                            .get(constructorIndex)
                            .getParameters().length);
        }

        //System.out.println("param index: " + parameterIndex);
        if (paramListSize != 0) {
            localTypeOfInterest = (o.getClass_constructors()
                    .get(constructorIndex)
                    .getParameters()[parameterIndex]
                    .getType().toString());
        } else {
            localTypeOfInterest = null;
        }

    }

}
