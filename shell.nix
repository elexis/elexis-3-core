#! /usr/bin/env nix-shell
# call it: nix-shell ./start_h2.sh
# mit GDK_BACKEND=wayland läuft copy/paste zu kate nicht
# install package at-spi2-core to avoid error
# AT-SPI: Error retrieving accessibility bus address: org.freedesktop.DBus.Error.ServiceUnknown: The name org.a11y.Bus was not provided by any .service files
with import <nixpkgs> {};
let mvn = pkgs.maven.override { jdk = pkgs.jdk17; };
in pkgs.mkShell {
  buildInputs = [ mvn pkgs.jq pkgs.jdk17 pkgs.ruby pkgs.rubyPackages.rugged];
 #   buildInputs = with pkgs; [ maven ruby_3_0 rubyPackages_3_0.rugged];
    NIX_LD_LIBRARY_PATH = lib.makeLibraryPath [
      zlib
      dbus
      git
      glib
      glib-networking
      gnulib
      gsettings-desktop-schemas
      gtk3
      nss nspr libdrm xorg.libXdamage mesa alsa-lib# for chromium
      swt
      gvfs
      jdk17
      librsvg
      libsecret
      libzip
      openssl
      stdenv
      stdenv.cc.cc
      unzip
      webkitgtk
      xorg.libXtst
    ];
    NIX_LD = builtins.readFile "${stdenv.cc}/nix-support/dynamic-linker";
    shellHook = ''
      export version=`echo ${gtk3}  | cut -d '-' -f 3`
      export GIO_MODULE_DIR=${glib-networking}/lib/gio/modules/
      echo GIO_MODULE_DIR are $GIO_MODULE_DIR
      export XDG_DATA_DIRS="$XDG_DATA_DIRS:${gtk3}/share/gsettings-schemas/gtk+3-$version:${at-spi2-core}/share/dbus-1/services" # this worked! Tested using File..Open
      export GDK_BACKEND=x11
      export GSETTINGS_SCHEMA_DIR=${at-spi2-core}/share/dbus-1/services
      echo GSETTINGS_SCHEMA_DIR are $GSETTINGS_SCHEMA_DIR
      echo done shellHook version is $version with GDK_BACKEND $GDK_BACKEND
    '';
  }
