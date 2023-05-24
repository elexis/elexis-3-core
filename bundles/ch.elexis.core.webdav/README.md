
# Webdav URL Handler base in Sardine

This URLHandler supports `webdav` support by implementing
url handlers for `dav` (http) (unencrypted, authenticated only for `localhost`)  and `davs` (https) respectively.

It uses the [Sardine library](https://github.com/lookfirst/sardine "title"). 

## Custom context based bearer authentication

In order to authentication using a bearer token, one can use the URL of the targeted service like this
`davs://$ctx.access-token@my.nextcloud.ch/`. Now before accessing the remote WebDav Url, the IContextService
is queried for its containment of an `AccessToken` which is then set as Bearer Authentication.