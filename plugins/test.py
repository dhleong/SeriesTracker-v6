#
# Test plugin for STv6
#

import stlib

#sess = stlib.JsonSession()
#result = {'res':sess['var1']}
#sess.end(result)

sess = stlib.Session()
for bytes in stlib.bytes_from_file('favicon.ico'):
    sess.append(bytes)
sess.header('Content-type', 'image/ico')
sess.end()
