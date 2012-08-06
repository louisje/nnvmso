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
  cursor.execute("truncate nnset_to_nnchannel")
  db = '/home/ubuntu/files/gae/ChannelSetChannel.sql3'
  for entity in AllEntities(db):
     channelId   = entity['channelId'] 
     createDate  = entity['createDate']
     seq         = entity['seq']              
     setId       = entity['channelSetId']     
     updateDate  = entity['updateDate']
     
     cursor.execute ("""
          INSERT INTO nnset_to_nnchannel (channelId, createDate, seq, setId, updateDate)
          VALUES (%s, %s, %s, %s, %s)
       """, (channelId, createDate, seq, setId, updateDate))
     conn.commit()
     
  cursor.close ()
  conn.close ()

except MySQLdb.Error, e:
  print "Error %d: %s" % (e.args[0], e.args[1])
  sys.exit (1)


