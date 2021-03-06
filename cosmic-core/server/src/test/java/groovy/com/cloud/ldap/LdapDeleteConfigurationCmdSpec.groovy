package groovy.com.cloud.ldap

import com.cloud.api.ServerApiException
import com.cloud.api.command.LdapDeleteConfigurationCmd
import com.cloud.api.response.LdapConfigurationResponse
import com.cloud.ldap.LdapManager
import com.cloud.legacymodel.exceptions.InvalidParameterValueException
import spock.lang.Specification


class LdapDeleteConfigurationCmdSpec extends Specification {

    def "Test failed response from execute"() {
        given: "We have an LdapManager and LdapDeleteConfigurationCmd"
        def ldapManager = Mock(LdapManager)
        ldapManager.deleteConfiguration(_) >> { throw new InvalidParameterValueException() }
        def ldapDeleteConfigurationCmd = new LdapDeleteConfigurationCmd(ldapManager)
        when: "LdapDeleteConfigurationCmd is executed and no configuration exists"
        ldapDeleteConfigurationCmd.execute()
        then: "An exception is thrown"
        thrown ServerApiException
    }

    def "Test getEntityOwnerId is 1"() {
        given: "We have an LdapManager and LdapDeleteConfigurationCmd"
        def ldapManager = Mock(LdapManager)
        def ldapDeleteConfigurationCmd = new LdapDeleteConfigurationCmd(ldapManager)
        when: "Get entity owner id is called"
        long ownerId = ldapDeleteConfigurationCmd.getEntityOwnerId()
        then: "1 is returned"
        ownerId == 1
    }

    def "Test successful response from execute"() {
        given: "We have an LdapManager and LdapDeleteConfigurationCmd"
        def ldapManager = Mock(LdapManager)
        ldapManager.deleteConfiguration(_) >> new LdapConfigurationResponse("localhost")
        def ldapDeleteConfigurationCmd = new LdapDeleteConfigurationCmd(ldapManager)
        when: "LdapDeleteConfigurationCmd is executed"
        ldapDeleteConfigurationCmd.execute()
        then: "The given configuration should be deleted and returned"
        ldapDeleteConfigurationCmd.responseObject.hostname == "localhost"
    }

    def "Test successful return of getCommandName"() {
        given: "We have an LdapManager and LdapDeleteConfigurationCmd"
        def ldapManager = Mock(LdapManager)
        def ldapDeleteConfigurationCmd = new LdapDeleteConfigurationCmd(ldapManager)
        when: "Get Command name is called"
        String commandName = ldapDeleteConfigurationCmd.getCommandName()
        then: "ldapconfigurationresponse is returned"
        commandName == "ldapconfigurationresponse"
    }
}
