# useful scripts
## sed/awk/grep
___
Show mount points for directories in /
```
ls -1 / | sort | while read dir; do echo -e "/$dir\t$(df /$dir --output=target | tail -1) "; done | awk -F"\t" ' { d[NR]=$1; m[NR]=$2; if (length($1) > dM) dM = length($1); if (length($2) > mM) mM = length($2); } END { fmt=sprintf("%%-%ds\t%%-%ds\n",dM,mM); for (i=1; i<=NR; i++) printf(fmt,d[i],m[i]) };'
```
Example output:
```ddd
/bin    /
/boot   /boot
/dev    /dev
/etc    /
/home   /
/lib    /
/lib64  /
/media  /
/mnt    /
/opt    /
/proc   /proc
/root   /
/run    /run
/sbin   /
/srv    /
/sys    /sys
/tmp    /
/u01    /
/usr    /
/var    /
```
___