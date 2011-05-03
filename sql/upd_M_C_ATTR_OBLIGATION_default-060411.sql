-- Created: 06 April 2011
-- author: Enriko Käsper
--
-- change the default value of Complex Attribute OBLIGATION field to 'O' - Optional

alter table M_COMPLEX_ATTR modify column `OBLIGATION` enum('M','O','C') NOT NULL default 'O';
