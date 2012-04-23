import MySQLdb
import sqlite3
from google.appengine.datastore import entity_pb
from google.appengine.api import datastore

def AllEntities(db):
    conn = sqlite3.connect(db)
    cursor = conn.cursor()
    cursor.execute('select id, value from result order by sort_key, id')
    for unused_entity_id, entity in cursor:
        entity_proto = entity_pb.EntityProto(contents=entity)
        yield datastore.Entity._FromPb(entity_proto)

#main
conn = MySQLdb.connect (host = "localhost",
                        user = "root",
                        passwd = "letlet",
                        charset = "utf8", 
                        use_unicode = True,                       
                        db = "nncloudtv_content")

try:

  cursor = conn.cursor ()
  cursor.execute("truncate category_to_nnset")
  db = '/home/ubuntu/files/gae/CategoryChannelSet.sql3'
  for entity in AllEntities(db):
     categoryId = entity['categoryId']  
     createDate = entity['createDate'] 
     setId      = entity['channelSetId']      
     updateDate = entity['updateDate'] 
     
     cursor.execute ("""
          INSERT INTO category_to_nnset (categoryId, createDate, setId, updateDate)
          VALUES (%s, %s, %s, %s)
       """, (categoryId, createDate, setId, updateDate))
     conn.commit()
     
  cursor.close ()
  conn.close ()

except MySQLdb.Error, e:
  print "Error %d: %s" % (e.args[0], e.args[1])
  sys.exit (1)

