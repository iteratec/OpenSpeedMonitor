databaseChangeLog = {
    changeSet(author: "dkl", id: "1494576018000-1") {
        addColumn(tableName: "job") {
            column(name: "option_is_private"                , type: "BOOLEAN")      {constraints(nullable: "false")}
            column(name: "option_block"                     , type: "varchar(255)")
            column(name: "option_iq"                        , type: "integer")
            column(name: "option_mobile"                    , type: "BOOLEAN")      {constraints(nullable: "false")}
            column(name: "option_uastring"                  , type: "varchar(255)")
            column(name: "option_dpr"                       , type: "integer")
            column(name: "option_cmdline"                   , type: "varchar(255)")
            column(name: "option_custom"                    , type: "varchar(255)")
            column(name: "option_tester"                    , type: "varchar(255)")
            column(name: "option_timeline"                  , type: "BOOLEAN")      {constraints(nullable: "false")}
            column(name: "option_timeline_stack"            , type: "integer")
            column(name: "option_mobile_device"             , type: "varchar(255)")
            column(name: "option_appendua"                  , type: "varchar(255)")
            column(name: "option_lighthouse"                , type: "BOOLEAN")      {constraints(nullable: "false")}
            column(name: "option_type"                      , type: "varchar(255)")
            column(name: "option_custom_headers"            , type: "varchar(255)")
            column(name: "option_trace"                     , type: "BOOLEAN")      {constraints(nullable: "false")}
            column(name: "option_spof"                      , type: "varchar(255)")

            column(defaultValue: "DEFAULT", name: "option_take_screenshots"           , type: "varchar(255)")
            column(defaultValue: "NONE"   , name: "option_save_bodies"                , type: "varchar(255)")
            column(defaultValue: "DEFAULT", name: "option_user_agent"                 , type: "varchar(255)")
        }
    }
}
