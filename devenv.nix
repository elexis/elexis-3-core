{ pkgs, lib, config, inputs, ... }:

{
  # https://devenv.sh/basics/
  env.LANG= "de_CH.UTF-8";
  env.LC_MESSAGES= "de_CH.UTF-8";
  env.LC_ALL= "de_CH.UTF-8";

  # https://devenv.sh/packages/
  packages = [ pkgs.git pkgs.maven ];

  # https://devenv.sh/tests/
  enterTest = ''
    echo "Running tests"
    git --version | grep --color=auto "${pkgs.git.version}"
    mvn -V clean verify  -Dtycho.localArtifacts=ignore
  '';

  # See full reference at https://devenv.sh/reference/options/
}
