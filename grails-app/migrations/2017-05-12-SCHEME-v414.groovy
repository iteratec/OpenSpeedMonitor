databaseChangeLog = {
    changeSet(author: "dkl", id: "1494576018000-1") {
        addColumn(tableName: "job") {
            column(defaultValueBoolean: "true",name: "is_private",type: "BOOLEAN"){constraints(nullable: "false")}
            column(name: "urls_to_block"             , type: "varchar(255)")
            column(name: "image_quality"             , type: "integer")
            column(name: "emulate_mobile"            , type: "BOOLEAN")      {constraints(nullable: "false")}
            column(name: "user_agent_string"         , type: "varchar(255)")
            column(name: "device_pixel_ration"       , type: "integer")
            column(name: "cmdline_options"           , type: "varchar(255)")
            column(name: "custom_metrics"            , type: "varchar(255)")
            column(name: "tester"                    , type: "varchar(255)")
            column(name: "capture_timeline"          , type: "BOOLEAN")      {constraints(nullable: "false")}
            column(name: "javascript_callstack"      , type: "integer")
            column(name: "mobile_device"             , type: "varchar(255)")
            column(name: "append_user_agent"         , type: "varchar(255)")
            column(name: "perform_lighthouse_test"   , type: "BOOLEAN")      {constraints(nullable: "false")}
            column(name: "optional_test_types"       , type: "varchar(255)")
            column(name: "custom_headers"            , type: "varchar(255)")
            column(name: "trace"                     , type: "BOOLEAN")      {constraints(nullable: "false")}
            column(name: "spof"                      , type: "varchar(255)")

            column(defaultValue: "DEFAULT", name: "take_screenshots"           , type: "varchar(255)")
            column(defaultValue: "NONE"   , name: "save_bodies"                , type: "varchar(255)")
            column(defaultValue: "DEFAULT", name: "user_agent"                 , type: "varchar(255)")
        }
    }
}
