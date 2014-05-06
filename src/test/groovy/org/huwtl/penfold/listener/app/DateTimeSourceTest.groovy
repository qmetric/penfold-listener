package org.huwtl.penfold.listener.app

import spock.lang.Specification

class DateTimeSourceTest extends Specification {

    def "should provide current datetime"()
    {
        expect:
        new DateTimeSource().now() != null
    }
}
