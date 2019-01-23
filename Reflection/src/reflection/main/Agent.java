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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
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

    public Agent() {
    }
    
    public void addWrappers(List<ClassWrapper> cw)
    {
        knownClasses_.addAll(cw);
    }
    
    public void exposeObject()
    {
        List<Object> evoObj = knownClasses_.stream()
                .filter(c -> c.isEvolutionary())
                .collect(Collectors.toList());
        
        Random rnd = new Random();
        
        ClassWrapper o = (ClassWrapper) evoObj.get(rnd.nextInt(evoObj.size()));
        
        System.out.println(o.getClass_constructors().get(1).getParameters()[0].getType());
    }
    
}
