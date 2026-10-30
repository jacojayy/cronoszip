use std::fs::File;
use std::io;
use std::os::fd::AsRawFd;
use std::path::Path;

pub use super::unix::{
    allocated_bytes, apply_mode, create_new_no_follow, create_symlink, name_problem,
    open_exclusive, open_existing_no_follow, path_len, same_filesystem, sync_dir,
};

/// Android still caps a path at `PATH_MAX` bytes; `.hatchzip-part` is added
/// to a file's path while it is written.
pub const MAX_PATH_LEN: usize = 4095 - ".hatchzip-part".len();

/// Android storage is usually FUSE (emulated storage) or an SD card, where
/// `syncfs` is not dependable. Each file and folder is flushed instead.
pub const SYNC_EACH_FILE: bool = true;

pub fn sync_batch(output: &Path) -> io::Result<()> {
    File::open(output)?.sync_all()
}

/// Frees `len` bytes at `offset` without changing the file's length. FUSE
/// and FAT/exFAT refuse this, which simply disables streaming for `.tar.gz`
/// archives on those volumes.
pub fn punch_hole(file: &File, offset: u64, len: u64) -> io::Result<bool> {
    if len == 0 {
        return Ok(true);
    }
    let (Ok(off), Ok(len)) = (i64::try_from(offset), i64::try_from(len)) else {
        return Err(io::ErrorKind::InvalidInput.into());
    };
    loop {
        let mode = libc::FALLOC_FL_PUNCH_HOLE | libc::FALLOC_FL_KEEP_SIZE;
        if unsafe { libc::fallocate64(file.as_raw_fd(), mode, off, len) } == 0 {
            return Ok(true);
        }
        let e = io::Error::last_os_error();
        match e.raw_os_error() {
            Some(libc::EINTR) => continue,
            Some(libc::EOPNOTSUPP | libc::ENOSYS | libc::EINVAL) => return Ok(false),
            _ => return Err(e),
        }
    }
}

const BTRFS_MAGIC: i64 = 0x9123_683E;
const ZFS_MAGIC: i64 = 0x2FC1_2FC1;

/// The most of a punched range the filesystem may keep allocated at each end.
/// exFAT and FUSE storage free whole clusters; 256 KiB covers the usual SD
/// card cluster sizes with room to spare.
pub fn punch_granule(dir: &Path) -> u64 {
    let Ok(f) = File::open(dir) else {
        return 128 << 20;
    };
    let mut st: libc::statfs = unsafe { std::mem::zeroed() };
    if unsafe { libc::fstatfs(f.as_raw_fd(), &mut st) } != 0 {
        return 128 << 20;
    }
    match st.f_type as i64 {
        BTRFS_MAGIC => 128 << 20,
        ZFS_MAGIC => 16 << 20,
        _ => (st.f_bsize as u64).max(256 << 10),
    }
}

/// An archive path as UnRAR's C interface takes it: a narrow string here.
#[cfg(feature = "rar")]
pub struct RarName(std::ffi::CString);

#[cfg(feature = "rar")]
impl RarName {
    pub fn new(path: &Path) -> io::Result<Self> {
        use std::os::unix::ffi::OsStrExt;
        std::ffi::CString::new(path.as_os_str().as_bytes())
            .map(Self)
            .map_err(|_| io::ErrorKind::InvalidInput.into())
    }

    pub fn open_data(&self, mode: std::ffi::c_uint) -> unrar_ng_sys::OpenArchiveDataEx {
        unrar_ng_sys::OpenArchiveDataEx::new(self.0.as_ptr(), mode)
    }
}
