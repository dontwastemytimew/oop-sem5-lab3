package com.lab.notes.data.sync;

import static org.junit.Assert.assertSame;

import com.lab.notes.model.Note;

import org.junit.Test;

public class LastWriteWinsResolverTest {

    private final LastWriteWinsResolver resolver = new LastWriteWinsResolver();

    @Test
    public void resolve_returnsRemoteNote_whenRemoteIsNewer() {
        Note local = new Note("id-1", "Title", "Local", 1000L);
        Note remote = new Note("id-1", "Title", "Remote", 2000L);

        assertSame(remote, resolver.resolve(local, remote));
    }

    @Test
    public void resolve_returnsLocalNote_whenLocalIsNewer() {
        Note local = new Note("id-1", "Title", "Local", 3000L);
        Note remote = new Note("id-1", "Title", "Remote", 2000L);

        assertSame(local, resolver.resolve(local, remote));
    }

    @Test
    public void resolve_returnsLocalNote_whenTimesAreEqual() {
        Note local = new Note("id-1", "Title", "Local", 2000L);
        Note remote = new Note("id-1", "Title", "Remote", 2000L);

        assertSame(local, resolver.resolve(local, remote));
    }
}