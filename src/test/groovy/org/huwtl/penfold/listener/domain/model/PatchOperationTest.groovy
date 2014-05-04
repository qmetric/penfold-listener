package org.huwtl.penfold.listener.domain.model

import com.google.common.base.Optional
import spock.lang.Specification

class PatchOperationTest extends Specification {

    def "should evaluate equality"() {
        expect:
        new Add("type1", "/a", new CustomDefinedValue(Optional.absent(), "abc")) == new Add("type1", "/a", new CustomDefinedValue(Optional.absent(), "abc"))
        new Add("type1", "/b", new CustomDefinedValue(Optional.absent(), "abc")) != new Add("type1", "/a", new CustomDefinedValue(Optional.absent(), "abc"))
        new Add("type1", "/a", new CustomDefinedValue(Optional.absent(), "def")) != new Add("type1", "/a", new CustomDefinedValue(Optional.absent(), "abc"))
    }
}
