/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
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
 * @author woaf
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
