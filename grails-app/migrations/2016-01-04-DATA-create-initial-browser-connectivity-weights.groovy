databaseChangeLog = {
    changeSet(author: "nkuhn", id: "1451911470-1") {
        sql('''
            INSERT INTO browser_connectivity_weight (version,browser_id,connectivity_id,weight)
            SELECT 0,b.id, c.id, b.weight
            FROM browser b CROSS JOIN connectivity_profile c
            WHERE b.weight > 0;
        ''' )
    }
}