databaseChangeLog = {
    changeSet(author: "pal", id: "OttoTask-1-findHetznerAndNetlab") {
        sql('''
            UPDATE location
            SET device_type = 'DESKTOP'
            WHERE LOWER(label) REGEXP '.*(-win|ie\\s*[1-9]*|firefox|nuc|desktop|hetzner|netlab).*';
        ''')
    }

    changeSet(author: "pal", id: "OttoTask-2-findHetznerAndNetlab") {
        sql('''
            UPDATE location
            SET operating_system = 'WINDOWS'
            WHERE LOWER(label) REGEXP '.*(-win|ie\\s*[1-9]*|firefox|nuc|desktop|hetzner|netlab).*';
        ''')
    }

    changeSet(author: "pal", id: "OttoTask-3-findHetznerAndNetlab") {
        sql('''
            UPDATE event_result e
            SET e.device_type = (SELECT l.device_type FROM location l WHERE l.id = e.location_id), 
                e.operating_system = (SELECT l.operating_system FROM location l WHERE l.id = e.location_id);
        ''')
    }
}
