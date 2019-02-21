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
package base;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;
import lombok.Data;

/**
 *
 * @author Woaf
 */
@Data
@Entity
@Table(schema = "WOAF", name = "BLOCK")
public class Block {
    
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(nullable = false)
    private int id_;
    @Column(nullable = false)
    private int step_ = 1;
    @Column(nullable = false)
    private float width_;
    @Column(nullable = false)
    private float height_;
    @Column(nullable = false)
    private float depth_;
    @Column(nullable = false)
    private boolean groundAttached_;
    @Column(nullable = false)
    private boolean hollow;

    public Block() {
    }

    public Block(float width_, float height_, float depth_, boolean groundAttached_, boolean hollow) {
        this.width_ = width_;
        this.height_ = height_;
        this.depth_ = depth_;
        this.groundAttached_ = groundAttached_;
        this.hollow = hollow;
    }
    
}
