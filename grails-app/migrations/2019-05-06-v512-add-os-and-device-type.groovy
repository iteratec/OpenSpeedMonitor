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
            SET operating_system = 'UNKNOWN'
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
            SET operating_system = 'UNKNOWN'
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
            WHERE LOWER(label) REGEXP '.*(-win|ie\\s*[1-9]*|firefox|nuc).*';
        ''')
    }

    changeSet(author: "pal", id: "Task-6_populate_device_type_location") {
        sql('''
            UPDATE location
            SET device_type = 'TABLET'
            WHERE LOWER(label) REGEXP '.*(pad|tab|note|xoom|book|tablet).*';
        ''')
    }

    changeSet(author: "pal", id: "Task-7_populate_device_type_location") {
        sql('''
            UPDATE location
            SET device_type = 'SMARTPHONE'
            WHERE LOWER(label) REGEXP '.*(samsung|moto|sony|nexus|huawei|nokia|alcatel|lg|oneplus|htc|phone).*' AND NOT device_type = 'TABLET';
        ''')
    }

    changeSet(author: "pal", id: "Task-8_populate_operating_system_location") {
        sql('''
            UPDATE location
            SET operating_system = 'WINDOWS'
            WHERE LOWER(label) REGEXP '.*(-win|ie\\s*[1-9]*|firefox|nuc).*';
        ''')
    }

    changeSet(author: "pal", id: "Task-10_populate_operating_system_location") {
        sql('''
            UPDATE location
            SET operating_system = 'ANDROID'
            WHERE LOWER(label) REGEXP '.*(samsung|moto|sony|nexus|huawei|nokie|lg|htc|alcatel|oneplus).*';
        ''')
    }

    changeSet(author: "pal", id: "Task-9_populate_operating_system_location") {
        sql('''
            UPDATE location
            SET operating_system = 'IOS'
            WHERE LOWER(label) REGEXP '.*(ios|iphone|ipad).*' AND NOT (operating_system = 'DESKTOP') AND NOT (operating_system = 'WINDOWS');
        ''')
    }

    changeSet(author: "pal", id: "Task-11_populate_device_type_event_result") {
        sql('''
            UPDATE event_result e
            SET e.device_type = (SELECT l.device_type FROM location l WHERE l.id = e.location_id), 
                e.operating_system = (SELECT l.operating_system FROM location l WHERE l.id = e.location_id);
        ''')
    }
}