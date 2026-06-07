-- Create a dedicated public bucket for profile photos.
-- Objects are stored under "<public.users.id>/<file>".

insert into storage.buckets (
  id,
  name,
  public,
  file_size_limit,
  allowed_mime_types
)
values (
  'profile-photos',
  'profile-photos',
  true,
  5242880,
  array['image/jpeg', 'image/png', 'image/webp', 'image/gif']
)
on conflict (id) do update
set
  public = excluded.public,
  file_size_limit = excluded.file_size_limit,
  allowed_mime_types = excluded.allowed_mime_types;

drop policy if exists "Profile photos are publicly readable" on storage.objects;
drop policy if exists "Users can upload own profile photos" on storage.objects;
drop policy if exists "Users can update own profile photos" on storage.objects;
drop policy if exists "Users can delete own profile photos" on storage.objects;

create policy "Profile photos are publicly readable"
on storage.objects
for select
to public
using (bucket_id = 'profile-photos');

create policy "Users can upload own profile photos"
on storage.objects
for insert
to authenticated
with check (
  bucket_id = 'profile-photos'
  and (storage.foldername(name))[1] ~ '^[0-9]+$'
  and exists (
    select 1
    from public.users u
    where u.id = ((storage.foldername(name))[1])::integer
      and lower(u.email) = lower(auth.jwt() ->> 'email')
  )
);

create policy "Users can update own profile photos"
on storage.objects
for update
to authenticated
using (
  bucket_id = 'profile-photos'
  and (storage.foldername(name))[1] ~ '^[0-9]+$'
  and exists (
    select 1
    from public.users u
    where u.id = ((storage.foldername(name))[1])::integer
      and lower(u.email) = lower(auth.jwt() ->> 'email')
  )
)
with check (
  bucket_id = 'profile-photos'
  and (storage.foldername(name))[1] ~ '^[0-9]+$'
  and exists (
    select 1
    from public.users u
    where u.id = ((storage.foldername(name))[1])::integer
      and lower(u.email) = lower(auth.jwt() ->> 'email')
  )
);

create policy "Users can delete own profile photos"
on storage.objects
for delete
to authenticated
using (
  bucket_id = 'profile-photos'
  and (storage.foldername(name))[1] ~ '^[0-9]+$'
  and exists (
    select 1
    from public.users u
    where u.id = ((storage.foldername(name))[1])::integer
      and lower(u.email) = lower(auth.jwt() ->> 'email')
  )
);
