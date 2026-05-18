-- Allow admin dashboard users to read all public user profiles.
-- Without this policy, authenticated users only receive their own row.

drop policy if exists "Admins can read all users" on public.users;

create policy "Admins can read all users"
on public.users
for select
to authenticated
using (
  exists (
    select 1
    from public.users admin_user
    where admin_user.email = auth.jwt() ->> 'email'
      and upper(admin_user.role) = 'ADMIN'
  )
);
