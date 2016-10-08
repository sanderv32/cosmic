--;
-- Schema cleanup from 5.0.1 to 5.1.0;
--;
-- Remove OpenDayLight plugin
DROP TABLE IF EXISTS `cloud`.`external_opendaylight_controllers`;

-- Remove IAM plugin
DROP TABLE IF EXISTS `cloud`.`iam_policy_permission`;
DROP TABLE IF EXISTS `cloud`.`iam_account_policy_map`;
DROP TABLE IF EXISTS `cloud`.`iam_group_policy_map`;
DROP TABLE IF EXISTS `cloud`.`iam_policy`;
DROP TABLE IF EXISTS `cloud`.`iam_group_account_map`;
DROP TABLE IF EXISTS `cloud`.`iam_group`;

-- Remove Simulator related column on physical_network_traffic_types table
ALTER TABLE `cloud`.`physical_network_traffic_types` DROP COLUMN `simulator_network_label`;

-- Remove BareMetal plugin
UPDATE configuration SET `default_value` = 'KVM,XenServer,VMware' WHERE `name` = 'hypervisor.list';
DELETE FROM `cloud`.`configuration` WHERE `name` IN (
  'external.baremetal.system.url',
  'external.baremetal.resource.classname',
  'enable.baremetal.securitygroup.agent.echo',
  'interval.baremetal.securitygroup.agent.echo',
  'baremetal_dhcp_devices',
  'baremetal_dhcp_devices'
);
DROP TABLE IF EXISTS `cloud`.`baremetal_dhcp_devices`;
DROP TABLE IF EXISTS `cloud`.`baremetal_pxe_devices`;
DROP TABLE IF EXISTS `cloud`.`baremetal_rct`;

-- TMP!!!
-- Remove unused table async_job_journal
DROP TABLE IF EXISTS `cloud`.`async_job_journal`;

-- Remove unused table cluster_vsm_map
DROP TABLE IF EXISTS `cloud`.`cluster_vsm_map`;

-- Remove unused table cmd_exec_log
DROP TABLE IF EXISTS `cloud`.`cmd_exec_log`;

-- Remove unused table elastic_lb_vm_map
DROP TABLE IF EXISTS `cloud`.`elastic_lb_vm_map`;

-- Remove unused table network_asa1000v_map
DROP TABLE IF EXISTS `cloud`.`network_asa1000v_map`;

-- Remove unused table external_cisco_asa1000v_devices
DROP TABLE IF EXISTS `cloud`.`external_cisco_asa1000v_devices`;

-- Remove unused table external_cisco_vnmc_devices
DROP TABLE IF EXISTS `cloud`.`external_cisco_vnmc_devices`;

-- Remove unused table network_external_firewall_device_map
DROP TABLE IF EXISTS `cloud`.`network_external_firewall_device_map`;

-- Remove unused table external_firewall_devices
DROP TABLE IF EXISTS `cloud`.`external_firewall_devices`;
