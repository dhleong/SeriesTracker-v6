"""
STv6 Plugin Library
 By: Daniel Leong
 Created: 25 Mar 2011

For a JSON callback, the expected usage pattern is simple:

sess = stlib.JsonSession()      # begin the session
val = sess['var1']          # access passed args as a dictionary
result = {'var2', val + 1}  # store return values in a dictionary
result = val+1              # or just anything parsable by json.dumps
sess.end(result)            # pass result values (or None) to "end()"
"""

import sys
import json
import urlparse

class Session(object):
    """Session object for STv6 Plugins
    """

    headers  = {}
    response = ''

    def __init__(self):
        """Constructor takes no args"""

        # parse passed (GET) args
        raw = urlparse.parse_qsl(sys.argv[1])
        args = {}
        for name, val in raw:
            args[name] = val

        self.args = args

    def __getitem__(self, item):
        if self.args.has_key(item):
            return self.args[item]

        return ''

    def append(self, data):
        '''Append some data to the response'''
        self.response += data

    def end(self):
        '''Write headers'''
        self.header('Content-length', len(self.response))

        for k, v in self.headers.iteritems():
            sys.stdout.write("%s:%s\n" % (k, v))

        sys.stdout.write('\r\n')
        sys.stdout.write(self.response)

    def header(self, key, value):
        self.headers[key] = value

class JsonSession(Session):
    '''Subclass of Session specifically for returning
        JSON via a javascript callback'''

    # js callback name
    callback = 'callback'

    def __init__(self):
        super(JsonSession, self).__init__()

        if self.args.has_key('callback'):
            self.callback = self.args['callback']
            del self.args['callback']

        self.header('Content-type', 'application/json')

    def end(self, response):

        self.append( self.callback + "(")
        if response:
            self.append( json.dumps(response) )

        self.append( ")\n" )

        super(JsonSession, self).end()


def bytes_from_file(filename, chunksize=8192):
    '''Utility generator to get bytes from a file'''
    with open(filename, "rb") as f:
        while True:
            chunk = f.read(chunksize)
            if chunk:
                for b in chunk:
                    yield b
            else:
                break
