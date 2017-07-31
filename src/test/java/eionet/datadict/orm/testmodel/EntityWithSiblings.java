package eionet.datadict.orm.testmodel;

import javax.persistence.Id;
import javax.persistence.OneToOne;

public class EntityWithSiblings {

    @Id
    private Long id;
    
    @OneToOne
    private EntityWithSiblings sibling1;
    @OneToOne
    private EntityWithSiblings sibling2;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public EntityWithSiblings getSibling1() {
        return sibling1;
    }

    public void setSibling1(EntityWithSiblings sibling1) {
        this.sibling1 = sibling1;
    }

    public EntityWithSiblings getSibling2() {
        return sibling2;
    }

    public void setSibling2(EntityWithSiblings sibling2) {
        this.sibling2 = sibling2;
    }
    
}
