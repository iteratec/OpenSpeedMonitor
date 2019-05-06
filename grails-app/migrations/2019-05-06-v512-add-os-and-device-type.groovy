databaseChangeLog = {

    changeSet(author: "pal (generated)", id: "1556885789363-2") {
        addColumn(tableName: "event_result") {
            column(name: "device_type", type: "varchar(255)") {
                constraints(nullable: "false")
            }
        }
    }

    changeSet(author: "pal (generated)", id: "1556885789363-3") {
        addColumn(tableName: "location") {
            column(name: "device_type", type: "varchar(255)") {
                constraints(nullable: "false")
            }
        }
    }

    changeSet(author: "pal (generated)", id: "1556885789363-4") {
        addColumn(tableName: "event_result") {
            column(name: "operating_system", type: "varchar(255)") {
                constraints(nullable: "false")
            }
        }
    }

    changeSet(author: "pal (generated)", id: "1556885789363-5") {
        addColumn(tableName: "location") {
            column(name: "operating_system", type: "varchar(255)") {
                constraints(nullable: "false")
            }
        }
    }

    changeSet(author: "pal", id: "Task-1_populate_operating_system_location_init") {
        sql('''
            UPDATE location
            SET operating_system = 'UNKOWN'
        ''')
    }

    changeSet(author: "pal", id: "Task-2_populate_operating_system_location_init") {
        sql('''
            UPDATE location
            SET device_type = 'UNDEFINED'
        ''')
    }

    changeSet(author: "pal", id: "Task-3_populate_device_type_event_result_init") {
        sql('''
            UPDATE event_result
            SET operating_system = 'UNKOWN'
        ''')
    }

    changeSet(author: "pal", id: "Task-4_populate_device_type_event_result_init") {
        sql('''
            UPDATE event_result
            SET device_type = 'UNDEFINED'
        ''')
    }

    changeSet(author: "pal", id: "Task-5_populate_device_type_location") {
        sql('''
            UPDATE location
            SET device_type = 'DESKTOP'
            WHERE label REGEXP '(?i).*(-Win|IE\\s*[1-9]*|firefox|nuc).*';
        ''')
    }

    changeSet(author: "pal", id: "Task-6_populate_device_type_location") {
        sql('''
            UPDATE location
            SET device_type = 'TABLET'
            WHERE label REGEXP '(?i).*(Pad|Tab|Note|Xoom|Book|Tablet).*';
        ''')
    }

    changeSet(author: "pal", id: "Task-7_populate_device_type_location") {
        sql('''
            UPDATE location
            SET device_type = 'SMARTPHONE'
            WHERE label REGEXP '(?i)(?!(.*(Pad|Tab|Note|Xoom|Book|Tablet).*)).*(Samsung|Moto|Sony|Nexus|Huawei|Nokia|Alcatel|LG|OnePlus|HTC|Phone).*';
        ''')
    }

    changeSet(author: "pal", id: "Task-8_populate_operating_system_location") {
        sql('''
            UPDATE location
            SET operating_system = 'WINDOWS'
            WHERE label REGEXP '(?i).*(-Win|IE\\s*[1-9]*|firefox|nuc).*';
        ''')
    }

    changeSet(author: "pal", id: "Task-9_populate_operating_system_location") {
        sql('''
            UPDATE location
            SET operating_system = 'IOS'
            WHERE label REGEXP '(?i)(?!(.*(Android|Desktop).*)).*(ios|iphone|ipad).*';
        ''')
    }

    changeSet(author: "pal", id: "Task-10_populate_operating_system_location") {
        sql('''
            UPDATE location
            SET operating_system = 'ANDROID'
            WHERE label REGEXP '(?i).*(Samsung|Moto|Sony|Nexus|Huawei|Nokie|LG|HTC|Alcatel|OnePlus).*';
        ''')
    }
    changeSet(author: "pal", id: "Task-11_populate_device_type_event_result") {
        sql('''
               UPDATE event_result e, location l 
               SET e.device_type = l.device_type, e.operating_system = l.operating_system 
               WHERE e.location_id = l.id;
        ''')
    }
}