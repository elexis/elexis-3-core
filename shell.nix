# Helper script for Niklaus to be able to work under NixOS
# To get into a fish shell call in this directory: nix-shell --run fish
{ pkgs ? import <nixpkgs> {} }:
let mvn = pkgs.maven.override { jdk = pkgs.openjdk8; };
in pkgs.mkShell {
  buildInputs = [ mvn pkgs.jq pkgs.adoptopenjdk-hotspot-bin-8 pkgs.ruby pkgs.rubyPackages.rugged];
}
# Tested with the following commands after calling nix-shell
# mvn -V clean verify  -Dtycho.localArtifacts=ignore -DskipTests
# ./ch.elexis.core.releng/update_changelog.rb  --force-tag=release/3.8 --with-tickets
