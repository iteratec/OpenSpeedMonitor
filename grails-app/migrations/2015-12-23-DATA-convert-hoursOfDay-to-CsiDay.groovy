databaseChangeLog = {
    changeSet(author: "mmi", id: "1450880177365-2") {
        sql('''
            insert into csi_day (version, hour0weight, hour1weight, hour2weight, hour3weight, hour4weight, hour5weight, hour6weight, hour7weight, hour8weight,
                            hour9weight, hour10weight, hour11weight, hour12weight, hour13weight, hour14weight, hour15weight, hour16weight, hour17weight,
                            hour18weight, hour19weight, hour20weight, hour21weight, hour22weight, hour23weight)
            select * from (select 1, (select weight from hour_of_day where full_hour = 0),
                    (select weight from hour_of_day where full_hour = 1),
                    (select weight from hour_of_day where full_hour = 2),
                    (select weight from hour_of_day where full_hour = 3),
                    (select weight from hour_of_day where full_hour = 4),
                    (select weight from hour_of_day where full_hour = 5),
                    (select weight from hour_of_day where full_hour = 6),
                    (select weight from hour_of_day where full_hour = 7),
                    (select weight from hour_of_day where full_hour = 8),
                    (select weight from hour_of_day where full_hour = 9),
                    (select weight from hour_of_day where full_hour = 10),
                    (select weight from hour_of_day where full_hour = 11),
                    (select weight from hour_of_day where full_hour = 12),
                    (select weight from hour_of_day where full_hour = 13),
                    (select weight from hour_of_day where full_hour = 14),
                    (select weight from hour_of_day where full_hour = 15),
                    (select weight from hour_of_day where full_hour = 16),
                    (select weight from hour_of_day where full_hour = 17),
                    (select weight from hour_of_day where full_hour = 18),
                    (select weight from hour_of_day where full_hour = 19),
                    (select weight from hour_of_day where full_hour = 20),
                    (select weight from hour_of_day where full_hour = 21),
                    (select weight from hour_of_day where full_hour = 22),
                    (select weight from hour_of_day where full_hour = 23)) as temp where exists (select id from hour_of_day)
    ''')
    }
}