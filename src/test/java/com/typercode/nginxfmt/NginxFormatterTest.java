package com.typercode.nginxfmt;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class NginxFormatterTest {
    private final NginxFormatter formatter = new NginxFormatter();

    @Test
    void formatsBlocksAndPreservesHashComments() {
        String source = """
                http{# root comment
                server{
                listen 80;# port
                location /api { proxy_pass http://backend; # upstream
                }
                }
                }
                """;

        String expected = """
                http {
                    # root comment
                    server {
                        listen 80; # port
                        location /api {
                            proxy_pass http://backend; # upstream
                        }
                    }
                }
                """;

        assertEquals(expected, formatter.format(source));
    }

    @Test
    void keepsCommentOnlyLinesAndBlankLinesReadable() {
        String source = """
                # global

                events{worker_connections 1024;# max clients
                }
                """;

        String expected = """
                # global

                events {
                    worker_connections 1024; # max clients
                }
                """;

        assertEquals(expected, formatter.format(source));
    }

    @Test
    void doesNotTreatHashInsideQuotesAsCommentStart() {
        String source = """
                server{add_header X-Test "value # still text";# comment
                }
                """;

        String expected = """
                server {
                    add_header X-Test "value # still text"; # comment
                }
                """;

        assertEquals(expected, formatter.format(source));
    }
}
