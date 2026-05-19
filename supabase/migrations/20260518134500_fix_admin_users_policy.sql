-- Fix admin users read policy without recursively querying public.users in RLS.

drop policy if exists "Admins can read all users" on public.users;

create or replace function public.current_user_is_admin()
returns boolean
language sql
security definer
set search_path = public
as $$
  select exists (
    select 1
    from public.users
    where email = auth.jwt() ->> 'email'
      and upper(role) = 'ADMIN'
  );
$$;

grant execute on function public.current_user_is_admin() to authenticated;

create policy "Admins can read all users"
on public.users
for select
to authenticated
using (public.current_user_is_admin());
