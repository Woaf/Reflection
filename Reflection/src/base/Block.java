/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package base;

import javax.persistence.Entity;
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
    private int id;
    
}
