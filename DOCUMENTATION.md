## How to See Documentation In-Game

Add the following to main.ms to give yourself a /docs command that tabcompletes with functionnames and classes. 
For example, "/docs SKWorldGuard" will return a list of all the WorldGuard functions, 
but you can see all functions containing "sk" by typing "/docs sk" and hitting tab. 
"/docs sk_region_addmember" will give you the documentation for the sk_region_addmember function.

```
# I do this as a cache because there are a lot of functions,
# this way I only have to use get_functions() once
@gfuncs = get_functions();
export('gfuncs', @gfuncs);
export('funclasses', array_keys(@gfuncs));
@funcs = array();
foreach(@fa in @gfuncs) {
    foreach(@f in @fa) {
        @funcs[] = @f;
    }
}
export('funcs', @funcs);

register_command('docs', array(
    description: 'Shows the documentation for a given CommandHelper function',
    permission: 'commandhelper.*',
    noPermMsg: color('RED').'This is not the command you are looking for',
    usage: colorize('&aUsage&f: /docs [FunctionType|function_name]\n&aExample&f: /docs\n&aExample&f: /docs PlayerManagement\n&aExample&f: /docs ploc'),
    aliases: array('doc', 'func', 'funcs'),
    executor: closure(@al, @p, @args, @cmd) {
        @gfuncs = import('gfuncs');
        @fcs = import('funclasses');
        @funcs = import('funcs');
        if(array_size(@args) == 0) {
            msg(colorize('&aFunctionTypes&f: &e'.array_implode(@fcs, '&f, &e')));
            return(false);
        } else if (array_size(@args) == 1) {
            @choice = to_lower(@args[0]);
            foreach(@fc in @fcs) {
                if (@choice == to_lower(@fc)) {
                    msg(colorize('&6'.@fc.'&f: &b'.array_implode(@gfuncs[@fc], '&f, &b')));
                    return(true);
                }
            }
            if (array_contains(@funcs, @choice)) {
                msg(colorize('&2'.@choice.'&f:'));
                msg(colorize('&aReturn&f: '.reflect_docs(@choice, 'return')));
                msg(colorize('&aArgs&f: '.reflect_docs(@choice, 'args')));
                msg(colorize('&aDescription&f: '.reflect_docs(@choice, 'description')));
            } else {
                msg('You entered \''.@choice.'\'');
                msg('Use \'/docs\' to get a list of valid function names');
            }
        } else {
            return(false);
        }
    },
    tabcompleter: closure(@al, @p, @args, @cmd) {
        @funcs = import('funcs');
        @gfuncs = import('gfuncs');
        @fcs = import('funclasses');
        if(array_size(@args) == 1) {
            @c = to_lower(@args[0]);
            @returnable = array();
            foreach(@fc in @fcs) {
                if(string_position(to_lower(@fc), @c) != -1) {
                    @returnable[] = @fc;
                }
            }
            foreach(@f in @funcs) {
                if(string_position(@f, @c) != -1) {
                    @returnable[] = @f;
                }
            }
            return(@returnable);
        } else {
            return(array());
        }
    }
));

```
