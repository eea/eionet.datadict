
update M_ATTRIBUTE set SHORT_NAME='MinInclusiveValue',NAME='Minimum inclusive value' where SHORT_NAME='MinValue';
update M_ATTRIBUTE set SHORT_NAME='MaxInclusiveValue',NAME='Maximum inclusive value',DISP_ORDER=37 where SHORT_NAME='MaxValue';



insert into M_ATTRIBUTE (NAME,OBLIGATION,DEFINITION,SHORT_NAME,NAMESPACE_ID,DISP_TYPE,DISP_ORDER,DISP_WHEN,DISP_WIDTH,DISP_HEIGHT,DISP_MULTIPLE,INHERIT) values ('Minimum exclusive value', 'O', '', 'MinExclusiveValue', 3, 'text', 36, 1, 20, 1, 0, 0);
insert into M_ATTRIBUTE (NAME,OBLIGATION,DEFINITION,SHORT_NAME,NAMESPACE_ID,DISP_TYPE,DISP_ORDER,DISP_WHEN,DISP_WIDTH,DISP_HEIGHT,DISP_MULTIPLE,INHERIT) values ('Maximum exclusive value', 'O', '', 'MaxExclusiveValue', 3, 'text', 38, 1, 20, 1, 0, 0);

update M_ATTRIBUTE set DISP_ORDER=39 where SHORT_NAME='PublicOrInternal';



