#!/usr/bin/env python2.5

import cgi
import codecs
import os
import pprint
import shutil
import sys
import sqlite3

SCREENS = 0
COLUMNS = 4
ROWS = 4
CELL_SIZE = 110

CONTAINER_DESKTOP = -100

DIR = "db_files"
AUTO_FILE = DIR + "/launcher.db"
INDEX_FILE = DIR + "/index.html"

def usage():
  print "usage: print_db.py launcher.db <sw600|sw720> -- prints a launcher.db"
  print "usage: print_db.py <sw600|sw720> -- adb pulls a launcher.db from a device"
  print "       and prints it"
  print
  print "The dump will be created in a directory called db_files in cwd."
  print "This script will delete any db_files directory you have now"
  print "If copy db files denied, refer the comments above adb_root_remount func in print_db.py"


def make_dir():
  shutil.rmtree(DIR, True)
  os.makedirs(DIR)

# adb root can not run on most phones, use another way to do it:
# first, enter adb shell
# second, call su
# last, chmod 777 /data/data/com.cooeeui.brand.zenlauncher/databases/launcher.db
# and then print_db script can work
def adb_root_remount():
  os.system("adb root")
  os.system("adb remount")

def pull_file(fn):
  print "pull_file: " + fn
  rv = os.system("adb pull"
    + " /data/data/com.cooeeui.brand.zenlauncher/databases/launcher.db"
    + " " + fn);
  if rv != 0:
    print "adb pull failed"
    sys.exit(1)

def get_favorites(conn):
  c = conn.cursor()
  c.execute("SELECT * FROM favorites")
  columns = [d[0] for d in c.description]
  rows = []
  for row in c:
    rows.append(row)
  return columns,rows

def get_apps(conn):
  c = conn.cursor()
  c.execute("SELECT * FROM apps")
  columns = [d[0] for d in c.description]
  rows = []
  for row in c:
    rows.append(row)
  return columns,rows

def print_intent(out, id, i, cell):
  if cell:
    out.write("""<span class="intent" title="%s">shortcut</span>""" % (
        cgi.escape(cell, True)
      ))


def print_icon(out, id, i, cell):
  if cell:
    icon_fn = "icon_%d.png" % id
    out.write("""<img style="width: 3em; height: 3em;" src="%s">""" % ( icon_fn ))
    f = file(DIR + "/" + icon_fn, "w")
    f.write(cell)
    f.close()

def print_icon_type(out, id, i, cell):
  if cell == 0:
    out.write("Application (%d)" % cell)
  elif cell == 1:
    out.write("Shortcut (%d)" % cell)
  elif cell:
    out.write("%d" % cell)

def print_cell(out, id, i, cell):
  if not cell is None:
    out.write(cgi.escape(unicode(cell)))

FUNCTIONS = {
  "intent": print_intent,
  "icon": print_icon,
  "iconType": print_icon_type
}

def render_cell_info(out, cell, occupied):
  if cell is None:
    out.write("    <td width=%d height=%d></td>\n" %
        (CELL_SIZE, CELL_SIZE))
  elif cell == occupied:
    pass
  else:
    cellX = cell["cellX"]
    cellY = cell["cellY"]
    spanX = cell["spanX"]
    spanY = cell["spanY"]
    intent = cell["intent"]
    if intent:
      title = "title=\"%s\"" % cgi.escape(cell["intent"], True)
    else:
      title = ""
    out.write(("    <td colspan=%d rowspan=%d width=%d height=%d"
        + " bgcolor=#dddddd align=center valign=middle %s>") % (
          spanX, spanY,
          (CELL_SIZE*spanX), (CELL_SIZE*spanY),
          title))
    itemType = cell["itemType"]
    if itemType == 0:
      out.write("""<img style="width: 4em; height: 4em;" src="icon_%d.png">\n""" % ( cell["_id"] ))
      out.write("<br/>\n")
      out.write(cgi.escape(cell["title"]) + " <br/><i>(app)</i>")
    elif itemType == 1:
      out.write("""<img style="width: 4em; height: 4em;" src="icon_%d.png">\n""" % ( cell["_id"] ))
      out.write("<br/>\n")
      out.write(cgi.escape(cell["title"]) + " <br/><i>(shortcut)</i>")
    elif itemType == 2:
      out.write("""<i>folder</i>""")
    elif itemType == 4:
      out.write("<i>widget %d</i><br/>\n" % cell["appWidgetId"])
    else:
      out.write("<b>unknown type: %d</b>" % itemType)
    out.write("</td>\n")

def render_screen_info(out, screen):
  out.write("<tr>")
  out.write("<td>%s</td>" % (screen["_id"]))
  out.write("<td>%s</td>" % (screen["screenRank"]))
  out.write("</tr>")

def process_file(fn):
  global COUNT
  print "process_file: " + fn
  conn = sqlite3.connect(fn)
  columns,rows = get_favorites(conn)
  appCols, appRows = get_apps(conn)

  data = [dict(zip(columns,row)) for row in rows]
  appData = [dict(zip(appCols, appRow)) for appRow in appRows]

  # Calculate the proper number of columns, and rows in this db
  for d in data:
    if d["position"] is None:
      d["position"] = 0
    COUNT = max(COUNT, d["position"] + 1)

  out = codecs.open(INDEX_FILE, encoding="utf-8", mode="w")
  out.write("""<html>
<head>
<style type="text/css">
.intent {
  font-style: italic;
}
</style>
</head>
<body>
""")

  # Data table
  out.write("<b>Favorites table</b><br/>\n")
  out.write("""<html>
<table border=1 cellspacing=0 cellpadding=4>
<tr>
""")
  print_functions = []
  for col in columns:
    print_functions.append(FUNCTIONS.get(col, print_cell))
  for i in range(0,len(columns)):
    col = columns[i]
    out.write("""  <th>%s</th>
""" % ( col ))
  out.write("""
</tr>
""")

  for row in rows:
    out.write("""<tr>
""")
    for i in range(0,len(row)):
      cell = row[i]
      # row[0] is always _id
      out.write("""  <td>""")
      print_functions[i](out, row[0], row, cell)
      out.write("""</td>
""")
    out.write("""</tr>
""")
  out.write("""</table>
""")

  out.write("""
</body>
</html>
""")

  out.close()

def main(argv):
  if len(argv) == 1:
    make_dir()
    # disable adb root, see the comments on adb_root_remount definition.
    #adb_root_remount()
    pull_file(AUTO_FILE)
    process_file(AUTO_FILE)
  elif len(argv) == 2:
    make_dir()
    process_file(argv[1])
  else:
    usage()

if __name__=="__main__":
  main(sys.argv)
