PASSWD=/tmp/passwd-s3fs 
S3_BUCKET=${S3_BUCKET:-haas-data-dev}
S3_MOUNT=${S3_MOUNT:-/mnt/s3}

echo "$AWS_ACCESS_KEY_ID:$AWS_SECRET_ACCESS_KEY" >$PASSWD
chmod 600 $PASSWD
s3fs "$S3_BUCKET" "$S3_MOUNT" -o passwd_file=$PASSWD,umask=0007,uid=1000,gid=1000 
ls -l $MNT_POINT
