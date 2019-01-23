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
package evo;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;
import lombok.Data;

/**
 *
 * @author balin
 */
@Data
@Entity
@Table(schema="WOAF", name="TREE")
public class Tree {
    
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(nullable = false)
    private int id_;
    @Column(nullable = false)
    private int step_;
    @Column(nullable = false)
    private float height_;

    public Tree() {
        step_ = 1;
        height_ = 0;
    }

    public Tree(float height_) {
        step_ = 1;
        this.height_ = height_;
    }
    
    private float grow(){
        return (float) Math.log10(step_);
    }
    
    public float getHeight_()
    {
        return height_ + grow();
    }
    
}
